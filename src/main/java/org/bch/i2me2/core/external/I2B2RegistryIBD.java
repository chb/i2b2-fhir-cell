package org.bch.i2me2.core.external;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.util.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by CH176656 on 4/22/2015.
 * Import functionalities
 */

public class I2B2RegistryIBD extends WrapperAPI {
    private static String MODIFIER_MED_STATEMENT_STATUS = "statusStatement";
    private static String QUERY_IBD = "ibd_rxnorm_import.sql";
    private static String QUERY_RXCODES = "select rxcode,ibd_concept_cd from rxnorm_ibd";

    private static String INSERT_FACT = "insert into observation_fact" +
            "(patient_num, encounter_num, start_date, concept_cd, instance_num, modifier_cd, provider_id, tval_char, valtype_cd) values " +
            "(%s, %s, TO_DATE('%s','YYYY-MM-DD'), '%s', 1, '%s', '@', '%s', '%s')";

    private static String INSERT_PATIENT = "insert into patient_dimension (patient_num) values (%s)";
    private static String INSERT_ENCOUNTER = "insert into visit_dimension (encounter_num, patient_num) values (%s, %s)";
    private static String INSERT_PAT_MAPPING = "insert into patient_mapping " +
            "(patient_num, patient_ide, patient_ide_source) values" +
            "(%s, '%s', '%s')";

    private static String INSERT_ENC_MAPPING = "insert into encounter_mapping " +
            "(encounter_num, encounter_ide, encounter_ide_source) values " +
            "(%s, '%s', '%s')";

    private static String PRIOR_USE = "prior use";
    private static String CURRENT_USE = "current use";
    private static String FHIR_MAP_PRIOR_USE = "completed";
    private static String FHIR_MAP_CURRENT_USE = "in-progress";

    // TODO: Transactional control
    public void importIBD() throws Exception {

        // Open Connections
        Class.forName("oracle.jdbc.driver.OracleDriver");
        String authI2B2 = AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_DB_I2B2);
        String [] partsI2B2 = authI2B2.split(":");
        Connection conI2B2 = DriverManager.getConnection(AppConfig.getProp(AppConfig.I2B2_JDBC), partsI2B2[0], partsI2B2[1]);

        String authIBD = AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_DB_IBD);
        String [] partsIBD = authIBD.split(":");
        Connection conIBD = DriverManager.getConnection(AppConfig.getProp(AppConfig.IBD_JDBC), partsIBD[0], partsIBD[1]);

        StringBuffer sb = new StringBuffer();
        Utils.textFileToStringBuffer(I2B2RegistryIBD.class, QUERY_IBD,  sb, "\n");

        Statement stmt = conIBD.createStatement();
        Statement stmtQuery = conI2B2.createStatement();
        Statement stmtExec = conI2B2.createStatement();

        // Load all rxcodes with their associated conceptcd
        Map<String, String> mapRX = loadRXCodes(stmtQuery.executeQuery(QUERY_RXCODES));

        // Map to keep a link between patient_num and encounter_num
        Map<String, String> mapEncounter = new HashMap<>();

        // Map the real modifier codes
        Map<String, String> mapModifiers = AppConfig.getRealModifiersMap();
        String realModifierCD = mapModifiers.get(MODIFIER_MED_STATEMENT_STATUS);

        // Map the modifier values
        Map<String, String> mapValues = getValueMap();

        ResultSet rs = stmt.executeQuery(sb.toString());

        while(rs.next()) {
            String conceptCd = rs.getString("concept_path");
            String subjectId = rs.getString("subject_id");
            String startDate = rs.getString("start_date");
            String encounterNum= rs.getString("encounter_num");
            String name_char = rs.getString("name_char");

            mapEncounter.put(encounterNum, subjectId);
            //System.out.println(conceptCd + ", " + subjectId + "," + startDate + "," + encounterNum + "," + name_char);
            String rxCode = getRXCode(mapRX, conceptCd);
            String insFact = String.format(
                    INSERT_FACT,
                    subjectId,
                    encounterNum,
                    startDate.substring(0,10),
                    rxCode,
                    realModifierCD,
                    mapValues.get(name_char.trim().toLowerCase()),
                    'T');
            System.out.println(insFact);
            try {
                stmtExec.execute(insFact);
            } catch (Exception e) {
                this.log(Level.WARNING, "IMPORT ERROR:" + e.getMessage());
            }
        }
        rs.close();

        // Insert encounters
        Set<String>  keysEnc = mapEncounter.keySet();

        for(String enc: keysEnc) {
            String patientNum = mapEncounter.get(enc);
            String insertEnc = String.format(INSERT_ENCOUNTER, enc, patientNum);
            String insertEncMap = String.format(INSERT_ENC_MAPPING, enc, enc, AppConfig.getProp(AppConfig.I2B2_PDO_SOURCE_BCH));
            String insertEncMapSelf = String.format(INSERT_ENC_MAPPING, enc, enc, "HIVE");
            String insertPat = String.format(INSERT_PATIENT, patientNum);
            String insertPatMap = String.format(INSERT_PAT_MAPPING, patientNum, patientNum, AppConfig.getProp(AppConfig.I2B2_PDO_SOURCE_BCH));
            String insertPatMapSelf = String.format(INSERT_PAT_MAPPING, patientNum, patientNum, "HIVE");
            stmtExec.execute(insertEnc);
            stmtExec.execute(insertEncMap);
            stmtExec.execute(insertEncMapSelf);
            stmtExec.execute(insertPat);
            stmtExec.execute(insertPatMap);
            stmtExec.execute(insertPatMapSelf);
        }
        conI2B2.close();
        conIBD.close();
    }

    private Map<String, String> getValueMap() {
        Map<String, String> out = new HashMap<>();
        out.put(PRIOR_USE, FHIR_MAP_PRIOR_USE);
        out.put(CURRENT_USE, FHIR_MAP_CURRENT_USE);
        return out;
    }

    private String getRXCode(Map<String, String> mapRX, String conceptCD) {
        Set<String> keySet = mapRX.keySet();
        for(String key: keySet) {
            // We know it's ugly... TODO: refactor
            //System.out.println(conceptCD + " - " + "."+key+".");
            if (conceptCD.matches(".*"+key+".*")) return mapRX.get(key);
        }
        return null;
    }

    private Map<String, String> loadRXCodes(ResultSet rs) throws Exception {
        Map<String, String> out = new HashMap<>();
        while(rs.next()) {
            String rxcode = rs.getString("rxcode");
            String code_cd = rs.getString("ibd_concept_cd");
            System.out.println(rxcode + ":"+code_cd);
            out.put(code_cd, rxcode);
        }
        rs.close();
        return out;
    }
}
