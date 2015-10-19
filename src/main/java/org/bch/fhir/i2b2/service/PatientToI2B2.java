package org.bch.fhir.i2b2.service;

import ca.uhn.fhir.model.dstu2.composite.AddressDt;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import org.bch.fhir.i2b2.config.AppConfig;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.bch.fhir.i2b2.pdomodel.Element;
import org.bch.fhir.i2b2.pdomodel.ElementSet;
import org.bch.fhir.i2b2.pdomodel.PDOModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

/**
 * Created by ipinyol on 8/25/15.
 */
public class PatientToI2B2 extends FHIRToPDO {
    Logger log = LoggerFactory.getLogger(PatientToI2B2.class);
    public static final String SEP = "##";

    @Override
    public String getPDOXML(BaseResource resource) throws FHIRI2B2Exception {
        Patient patient = (Patient) resource;
        PDOModel pdo = new PDOModel();
        if (patient!=null) {
            this.patientIde = this.getPatiendIde(patient);
            ElementSet patientSet = this.generatePatientSet(patient);
            pdo.addElementSet(patientSet);
            // If patientSet is null, nothing to update
            if (patientSet == null) return null;
            ElementSet pidSet = this.generatePIDSet();
            pdo.addElementSet(pidSet);
        }

        return pdo.generatePDOXML();
    }
    // return null if bot zip code and state are not present
    protected ElementSet generatePatientSet(Patient patient) throws FHIRI2B2Exception {
        //<param column="sex_cd">F__PatientSegments.patientGender__F</param>
        ElementSet patientSet = new ElementSet();
        patientSet.setTypePDOSet(ElementSet.PDO_PATIENT_SET);
        Element patientElement = new Element();
        patientElement.setTypePDO(Element.PDO_PATIENT);

        String pdoPatientId = this.generateRow(PDOModel.PDO_PATIENT_ID, this.patientIde,
                genParamStr(PDOModel.PDO_SOURCE, this.patientIdeSource));
        patientElement.addRow(pdoPatientId);

        String address = this.getAddressInfo(patient);
        String zip = address.split(SEP,0)[0];
        String state = address.split(SEP,0)[1];
        //String zipElement = this.generateRow(PDOModel.PDO_COLUMN_ZIP_CD,zip);
        //String stateElement = this.generateRow(PDOModel.PDO_COLUMN_STATE_PATH,state);

        String zipElement = this.generateRow(PDOModel.PDO_PARAM, zip,
                genParamStr(PDOModel.PDO_COLUMN, PDOModel.PDO_COLUMN_ZIP_CD),
                genParamStr(PDOModel.PDO_TYPE, "string"));

        String stateElement = this.generateRow(PDOModel.PDO_PARAM, state,
                genParamStr(PDOModel.PDO_COLUMN, PDOModel.PDO_COLUMN_STATE_PATH),
                genParamStr(PDOModel.PDO_TYPE, "string"));

        boolean isInfoPresent=false;
        if (zip.length()!=0) {
            patientElement.addRow(zipElement);
            isInfoPresent = true;
        }
        if (state.length()!=0) {
            patientElement.addRow(stateElement);
            isInfoPresent = true;
        }
        if (!isInfoPresent) return null;

        // Provisional until CRCLoader works properly with patients that already exists
        boolean b=false;
        try {
            b = updateI2B2DB(zip, state, this.patientIde, this.patientIdeSource);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // No need to bother i2b2, since the upload as been done directly into i2b2
        if (b) return null;

        patientSet.addElement(patientElement);
        return patientSet;
    }

    // PRE: patient is not null
    private String getAddressInfo(Patient patient) {
        List<AddressDt> addrs = patient.getAddress();
        if (addrs.isEmpty()) {
            log.warn("No address information is provided for the Patient resource");
            return SEP;
        }
        AddressDt d = addrs.get(0);
        String zip = d.getPostalCode();
        String state = d.getState();

        return zip+SEP+state;
    }

    // PRE: patient is not null
    private String getPatiendIde(Patient patient) {
        return patient.getId().getIdPart();
    }

    /**
     * TODO: Refactor when I2B2 CRCLoader works properly with patients that already exists
     * This is a provisional method. Currently, the CRCLoader does not work when uploading patient information if the
     * patient is already there. So, we do it directly on DB.
     * @param zip
     * @param state
     * @param subjectId
     * @return
     * @throws Exception
     */

    // Return true if updated was done
    private boolean updateI2B2DB(String zip, String state, String subjectId, String source) throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        //String jdbcCon = AppConfig.getProp(AppConfig.I2B2_JDBC);
        //String auth = AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_FILE_DB_I2B2);
        //String[] auths = auth.split(":");

        Connection con = null;
        Statement stmt = null;
        boolean b = true;
        try {
            //con = DriverManager.getConnection(jdbcCon, auths[0], auths[1]);
            InitialContext context = new InitialContext();
            DataSource dataSource = (DataSource) context.lookup(AppConfig.getProp(AppConfig.I2B2_DATASOURCE));
            con = dataSource.getConnection();
            String numPatientSql = "select patient_num from patient_mapping where patient_ide = '" + subjectId +
                    "' and patient_ide_source='" + source + "'";

            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(numPatientSql);
            // It must be one entry
            if (!rs.next()) {
                b=false;
                rs.close();
            } else {
                String patientNum = rs.getString("patient_num");
                rs.close();

                String updateSql = "update patient_dimension set " + PDOModel.PDO_COLUMN_ZIP_CD + "='" + zip + "', " +
                        PDOModel.PDO_COLUMN_STATE_PATH + "='" + state + "' where patient_num=" + patientNum;
                stmt.executeUpdate(updateSql);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                if(stmt!=null) {
                    stmt.close();
                }
            } catch(SQLException se){
                se.printStackTrace();
            }// do nothing
            try{
                if(con!=null) {
                    con.close();
                }
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return b;
    }
}
