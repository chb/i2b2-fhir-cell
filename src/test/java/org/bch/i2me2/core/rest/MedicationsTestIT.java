package org.bch.i2me2.core.rest;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.util.HttpRequest;
import org.bch.i2me2.core.util.Response;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by CH176656 on 5/11/2015.
 */
public class MedicationsTestIT {
    public static String HTTP_TYPE_CONSUMES = "application/x-www-form-urlencoded";
    private HttpRequest httpRequest = new HttpRequest();

    // It requires the IDM system up with a valid token pointing to a patient whose subject Id is 999999999
    // It requires an active i2b2 crc cell
    @Test
    public void putMedicationsNew_simpleIT() throws Exception {
        String json = readTextFile("mrNewJSON0.json");
        String patientId = "999999999";
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE0Mjk3MjUxNzk1NjksInN1YmplY3RJZCI6MTk2NjA4LCJqdGkiOiJhNmZkYjQ4ZC03OGJiLTQ1ZmQtOWMyZi0wYTVmMTczYWExZGMiLCJpYXQiOjE0Mjk3MjQ4Nzk1Njl9.fha9SOVg6L4tKZLRd1wod0Rzg-01i2X7h3cnvZHIJ3nXwgwpVJ5E4LxjBvweRfoKwhL2zxlj7BE1484qGB8fug";
        String credentials = "MedRec2:MedRecApp1_";
        String encoding = javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes("UTF-8"));
        String auth = "Basic " + encoding;
        String url = "http://127.0.0.1:8080/i2me2/rest/medications/putMedications";
        String content = "token=" + token + "&" + "content=" + URLEncoder.encode(json, "UTF-8");
        Response resp = httpRequest.doPostGeneric(url, content, auth, HTTP_TYPE_CONSUMES);
        assertEquals(200, resp.getResponseCode());

        validateDataPutMedicationsNew(patientId);
    }

    private void validateDataPutMedicationsNew(String patientId) throws Exception {
        // Check that the DB has been populated properly
        Class.forName("oracle.jdbc.driver.OracleDriver");
        String jdbcCon = AppConfig.getProp(AppConfig.I2B2_JDBC);
        String auth = AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_DB_I2B2);
        String[] auths = auth.split(":");
        Connection con = DriverManager.getConnection(jdbcCon, auths[0], auths[1]);
        String query = "Select patient_num from patient_mapping where patient_ide='" + patientId + "' " +
                "and patient_ide_source='" + AppConfig.getProp(AppConfig.I2B2_PDO_SOURCE_BCH) + "'";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        // It must be one entry
        rs.next();
        String patientNum = rs.getString("patient_num");

        String query2 = "Select concept_cd, modifier_cd, tval_char, nval_num, units_cd, encounter_num, instance_num " +
                "from observation_fact " +
                "where patient_num=" + patientNum;
        stmt = con.createStatement();
        rs = stmt.executeQuery(query2);
        int i = 0;
        String encounterNum = "0";
        boolean[] bool = {false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};

        while (rs.next()) {
            i++;
            encounterNum = rs.getString("encounter_num");
            String modifierCd = rs.getString("modifier_cd");
            String tvalChar = rs.getString("tval_char");
            String nvalNum = rs.getString("nval_num");
            String conceptCd = rs.getString("concept_cd");
            String unitsCd = rs.getString("units_cd");
            String instanceNum = rs.getString("instance_num");
            System.out.println(encounterNum + ", " + modifierCd + ", " + tvalChar + ", " + nvalNum + ", " + conceptCd + ", " + unitsCd);
            if (modifierCd.trim().equals(AppConfig.getRealModifiersMap().get("medicationNameStatement"))) {
                assertEquals("CODETEXT", tvalChar.trim().toUpperCase());
                assertEquals("RXNORM:310798".toLowerCase(), conceptCd.trim().toLowerCase());
                assertEquals("1", instanceNum);
                bool[0] = true;
            } else if (modifierCd.trim().equals(AppConfig.getRealModifiersMap().get("statusStatement"))) {
                assertEquals("in-progress", tvalChar.trim().toLowerCase());
                assertEquals("RXNORM:310798".toLowerCase(), conceptCd.trim().toLowerCase());
                assertEquals("1", instanceNum);
                bool[1] = true;
            } else if (modifierCd.trim().equals(AppConfig.getRealModifiersMap().get("wasNotGivenStatement"))) {
                assertEquals("false".toLowerCase(), tvalChar.trim().toLowerCase());
                assertEquals("RXNORM:310798".toLowerCase(), conceptCd.trim().toLowerCase());
                assertEquals("1", instanceNum);
                bool[2] = true;
            } else if (modifierCd.trim().equals(AppConfig.getRealModifiersMap().get("dosageTextStatement"))) {
                assertEquals("RXNORM:310798".toLowerCase(), conceptCd.trim().toLowerCase());
                if (instanceNum.equals("1")) {
                    bool[3] = true;
                    assertEquals("TAKE 1 TABLET AT BEDTIME AS NEEDED FOR SLEEP1".toLowerCase(), tvalChar.trim().toLowerCase());
                } else if(instanceNum.equals("2")) {
                    bool[4] = true;
                    assertEquals("TAKE 1 TABLET AT BEDTIME AS NEEDED FOR SLEEP5".toLowerCase(), tvalChar.trim().toLowerCase());
                } else if (instanceNum.equals("3")) {
                    bool[5] = true;
                    assertEquals("TAKE 1 TABLET AT BEDTIME AS NEEDED FOR SLEEP7".toLowerCase(), tvalChar.trim().toLowerCase());
                }
            } else if (modifierCd.trim().equals(AppConfig.getRealModifiersMap().get("quantityValueStatement"))) {
                assertEquals("RXNORM:310798".toLowerCase(), conceptCd.trim().toLowerCase());
                if (instanceNum.equals("1")) {
                    bool[6] = true;
                    assertEquals("30".toLowerCase(), nvalNum.trim().toLowerCase());
                } else if(instanceNum.equals("2")) {
                    bool[7] = true;
                    assertEquals("40".toLowerCase(), nvalNum.trim().toLowerCase());
                } else if (instanceNum.equals("3")) {
                    bool[8] = true;
                    assertEquals("50".toLowerCase(), nvalNum.trim().toLowerCase());
                }
            } else if (modifierCd.trim().equals(AppConfig.getRealModifiersMap().get("countStatement"))) {
                assertEquals("RXNORM:310798".toLowerCase(), conceptCd.trim().toLowerCase());
                if (instanceNum.equals("1")) {
                    bool[9] = true;
                    assertEquals("1".toLowerCase(), nvalNum.trim().toLowerCase());
                } else if(instanceNum.equals("2")) {
                    bool[10] = true;
                    assertEquals("5".toLowerCase(), nvalNum.trim().toLowerCase());
                } else if (instanceNum.equals("3")) {
                    bool[11] = true;
                    assertEquals("7".toLowerCase(), nvalNum.trim().toLowerCase());
                }
            } else if (modifierCd.trim().equals(AppConfig.getRealModifiersMap().get("repeatWhenStatement"))) {
                assertEquals("RXNORM:310798".toLowerCase(), conceptCd.trim().toLowerCase());
                if (instanceNum.equals("1")) {
                    bool[12] = true;
                    assertEquals("CD".toLowerCase(), tvalChar.trim().toLowerCase());
                } else if(instanceNum.equals("2")) {
                    bool[13] = true;
                    assertEquals("ICM".toLowerCase(), tvalChar.trim().toLowerCase());
                } else if (instanceNum.equals("3")) {
                    bool[14] = true;
                    assertEquals("CV".toLowerCase(), tvalChar.trim().toLowerCase());
                }
            }

        }

        // There are 15 registers
        assertEquals(15, i);
        for (int j = 0; j < 15; j++) {
            assertTrue(bool[j]);
        }
        // Great, if we are here, everything is in its place. Lets remove all data now
        String queryDel = "delete from observation_fact where patient_num=" + patientNum;
        String queryDel2 = "delete from patient_mapping where patient_num=" + patientNum;
        String queryDel3 = "delete from encounter_mapping where encounter_num=" + encounterNum;
        String queryDel4 = "delete from patient_dimension where patient_num=" + patientNum;
        String queryDel5 = "delete from visit_dimension where encounter_num=" + encounterNum;
        stmt = con.createStatement();
        stmt.execute(queryDel);
        stmt.execute(queryDel2);
        stmt.execute(queryDel3);
        stmt.execute(queryDel4);
        stmt.execute(queryDel5);
        con.close();
    }

    private String readTextFile(String fileName) throws Exception {
        InputStream in = MedicationsIT.class.getResourceAsStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sBuffer = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sBuffer.append(line).append('\n');
            }
        } catch(Exception e) {
            e.printStackTrace();

        } finally {
            in.close();
        }
        return sBuffer.toString();
    }
}
