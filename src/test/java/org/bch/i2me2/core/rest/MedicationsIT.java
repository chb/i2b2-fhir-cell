package org.bch.i2me2.core.rest;

import org.apache.http.client.methods.HttpPost;
import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.external.I2B2QueryService;
import org.bch.i2me2.core.util.HttpRequest;
import org.bch.i2me2.core.util.Response;
import org.bch.i2me2.core.util.Utils;
import org.bch.i2me2.core.util.mapper.Mapper;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by CH176656 on 4/14/2015.
 */
@RunWith(Arquillian.class)
public class MedicationsIT extends AbstractRestIT {
    public static String HTTP_TYPE_CONSUMES = "application/x-www-form-urlencoded";

    @Inject
    private HttpRequest httpRequest;

    @Test
    public void getMedicationsByPassIT() throws Exception {
        String patientId = "1234";
        String credentials = "MedRec2:MedRecApp1_";
        String encoding =  javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes("UTF-8"));
        String auth = "Basic " + encoding;
        String url = "http://127.0.0.1:8080/i2me2/rest/medications/getMedicationsByPass/"+patientId;
        Response resp = httpRequest.doPostGeneric(url, auth);
        assertEquals(200,resp.getResponseCode());
        String xml = resp.getContent();
        I2B2QueryService.QueryResponse respXml = new I2B2QueryService.QueryResponse(xml);
        NodeList list = respXml.getAllObservations();
        assertEquals(2, list.getLength());
    }

    @Test
    public void getMedicationsByPass_2IT() throws Exception {
        String patientId = "123456";
        String credentials = "MedRec2:MedRecApp1_";
        String encoding =  javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes("UTF-8"));
        String auth = "Basic " + encoding;
        String url = "http://127.0.0.1:8080/i2me2/rest/medications/getMedicationsByPass/"+patientId;
        Response resp = httpRequest.doPostGeneric(url, auth);
        assertEquals(200,resp.getResponseCode());
        String xml = resp.getContent();

        I2B2QueryService.QueryResponse respXml = new I2B2QueryService.QueryResponse(xml);
        NodeList list = respXml.getAllObservations();
        assertEquals(32, list.getLength());
    }

    // It requires the IDM system up with a valid token pointing to a patient whose subject Id is 565656.
    // It requires an active i2b2 crc cell
    @Test
    public void getMedications_simpleIT() throws Exception {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE0Mjk3MTg0Mjc0MDUsInN1YmplY3RJZCI6NjU1MzcsImp0aSI6IjgxZjQyYTNmLTFmMzYtNDNmZS1iMDZjLTQzZDIwMDNlZThiYiIsImlhdCI6MTQyOTcxODEyNzQwNX0.qGQT2Lk30hn3vBNuZ9xvhovwto0FA-GoIbauQE8h75_0abaCJU7_2Wt35_hJUk98OLSQrHbjZJf6Ewe5o2J8zw";
        String credentials = "MedRec2:MedRecApp1_";
        String encoding =  javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes("UTF-8"));
        String auth = "Basic " + encoding;
        String url = "http://127.0.0.1:8080/i2me2/rest/medications/getMedications";
        String content = "token=" + token;
        Response resp = httpRequest.doPostGeneric(url, content, auth, HTTP_TYPE_CONSUMES);
        assertEquals(200,resp.getResponseCode());
        String xml = resp.getContent();

        I2B2QueryService.QueryResponse respXml = new I2B2QueryService.QueryResponse(xml);
        NodeList list = respXml.getAllObservations();
        assertEquals(32, list.getLength());
    }

    // It requires the IDM system up with a valid token pointing to a patient whose subject Id is 999999999
    // It requires an active i2b2 crc cell
    @Test
    public void putMedications_simpleIT() throws Exception {
        String json = readTextFile("mrJSON0.json");
        String patientId = "999999999";
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE0Mjk3MjUxNzk1NjksInN1YmplY3RJZCI6MTk2NjA4LCJqdGkiOiJhNmZkYjQ4ZC03OGJiLTQ1ZmQtOWMyZi0wYTVmMTczYWExZGMiLCJpYXQiOjE0Mjk3MjQ4Nzk1Njl9.fha9SOVg6L4tKZLRd1wod0Rzg-01i2X7h3cnvZHIJ3nXwgwpVJ5E4LxjBvweRfoKwhL2zxlj7BE1484qGB8fug";
        String credentials = "MedRec2:MedRecApp1_";
        String encoding =  javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes("UTF-8"));
        String auth = "Basic " + encoding;
        String url = "http://127.0.0.1:8080/i2me2/rest/medications/putMedications";
        String content = "token=" + token + "&" + "content=" + URLEncoder.encode(json, "UTF-8");
        Response resp = httpRequest.doPostGeneric(url, content, auth,HTTP_TYPE_CONSUMES);
        assertEquals(200,resp.getResponseCode());

        validateDataPutMedications(patientId);
    }

    @Test
    public void putMedicationsByPassIT() throws Exception {
        String json = readTextFile("mrJSON0.json");
        String patientId = "999999999";
        String credentials = "MedRec2:MedRecApp1_";
        String encoding =  javax.xml.bind.DatatypeConverter.printBase64Binary(credentials.getBytes("UTF-8"));
        String auth = "Basic " + encoding;
        String url = "http://127.0.0.1:8080/i2me2/rest/medications/putMedicationsByPass/"+patientId;
        Response resp = httpRequest.doPostGeneric(url, json, auth,"application/json");
        assertEquals(200,resp.getResponseCode());

        validateDataPutMedications(patientId);
    }

    private void validateDataPutMedications(String patientId) throws Exception {
        // Check that the DB has been populated properly
        Class.forName("oracle.jdbc.driver.OracleDriver");
        String jdbcCon = AppConfig.getProp(AppConfig.I2B2_JDBC);
        String auth = AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_DB_I2B2);
        String []auths = auth.split(":");
        Connection con = DriverManager.getConnection(jdbcCon, auths[0], auths[1]);
        String query = "Select patient_num from patient_mapping where patient_ide='" + patientId + "' " +
                "and patient_ide_source='" + AppConfig.getProp(AppConfig.I2B2_PDO_SOURCE_BCH) + "'";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        // It must be one entry
        rs.next();
        String patientNum = rs.getString("patient_num");

        String query2 = "Select concept_cd, modifier_cd, tval_char, nval_num, units_cd, encounter_num " +
                "from observation_fact " +
                "where patient_num="+ patientNum;
        stmt = con.createStatement();
        rs = stmt.executeQuery(query2);
        int i=0;
        String encounterNum="0";
        boolean [] bool = {false,false,false,false,false, false};

        while(rs.next()) {
            i++;
            encounterNum = rs.getString("encounter_num");
            String modifierCd = rs.getString("modifier_cd");
            String tvalChar = rs.getString("tval_char");
            String nvalNum = rs.getString("nval_num");
            String conceptCd = rs.getString("concept_cd");
            String unitsCd = rs.getString("units_cd");
            System.out.println(encounterNum + ", " + modifierCd + ", " + tvalChar + ", " + nvalNum + ", " + conceptCd + ", " + unitsCd);
            if (modifierCd.trim().equals(AppConfig.getRealModifiersMap().get("recsReturnedStatement"))) {
                assertEquals("all", tvalChar.trim().toLowerCase());
                assertEquals("PBM_transaction".toLowerCase(), conceptCd.trim().toLowerCase());
                bool[0] = true;
            } else if (modifierCd.trim().equals(AppConfig.getRealModifiersMap().get("statusMsgStatement"))) {
                assertEquals("ok", tvalChar.trim().toLowerCase());
                assertEquals("PBM_transaction".toLowerCase(), conceptCd.trim().toLowerCase());
                bool[1] = true;
            } else if (modifierCd.trim().equals(AppConfig.getRealModifiersMap().get("medicationNameStatement"))) {
                assertEquals("MEDICATION ONE", tvalChar.trim().toUpperCase());
                assertEquals("NDC:6666777".toLowerCase(), conceptCd.trim().toLowerCase());
                bool[2] = true;
            } else if (modifierCd.trim().equals(AppConfig.getRealModifiersMap().get("quantityValueStatement"))) {
                assertEquals("10", nvalNum.trim());
                assertEquals("NDC:6666777".toLowerCase(), conceptCd.trim().toLowerCase());
                bool[3] = true;
            } else if (modifierCd.trim().equals(AppConfig.getRealModifiersMap().get("quantityCodeStatement"))) {
                assertEquals("ml".toLowerCase(), tvalChar.trim().toLowerCase());
                assertEquals("NDC:6666777".toLowerCase(), conceptCd.trim().toLowerCase());
                bool[4] = true;
            }  else if (modifierCd.trim().equals(AppConfig.getRealModifiersMap().get("expectedDurationStatement"))) {
                assertEquals("20", nvalNum.trim());
                assertEquals("days".toLowerCase(), unitsCd);
                assertEquals("NDC:6666777".toLowerCase(), conceptCd.trim().toLowerCase());
                bool[5] = true;
            }
        }

        // There are 6 registers
        assertEquals(6,i);
        for(int j=0;j<6;j++) {
            assertTrue(bool[j]);
        }
        // Great, if we are here, everything is in its place. Lets remove all data now
        String queryDel = "delete from observation_fact where patient_num="+patientNum;
        String queryDel2 = "delete from patient_mapping where patient_num="+patientNum;
        String queryDel3 = "delete from encounter_mapping where encounter_num="+encounterNum;
        String queryDel4 = "delete from patient_dimension where patient_num="+patientNum;
        String queryDel5 = "delete from visit_dimension where encounter_num="+encounterNum;
        stmt = con.createStatement();
        stmt.execute(queryDel);
        stmt.execute(queryDel2);
        stmt.execute(queryDel3);
        stmt.execute(queryDel4);
        stmt.execute(queryDel5);
        con.close();
    }

    @Test
    public void testJDBC_IT() throws Exception {

        String query = "Select patient_num from patient_mapping where patient_ide='1234'";
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection con = DriverManager.getConnection("jdbc:oracle:thin:@10.17.16.148:1521:xe", "idmuser", "idmuser");
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            String patientNum = rs.getString("patient_num");
            System.out.println(patientNum);
        }
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
