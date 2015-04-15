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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by CH176656 on 4/14/2015.
 */
@RunWith(Arquillian.class)
public class MedicationsIT extends AbstractRestIT {

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

        // Check that the DB has been populated properly
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection con = DriverManager.getConnection("jdbc:oracle:thin:@10.17.16.148:1521:xe", "idmuser", "idmuser");
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
            encounterNum = rs.getNString("encounter_num");
            String modifierCd =  rs.getNString("modifier_cd");
            String tvalChar = rs.getNString("tval_char");
            String nvalNum = rs.getNString("nval_num");
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
