package org.bch.i2me2.core.rest;

import static org.junit.Assert.*;

import javax.ws.rs.core.Response;

import org.bch.i2me2.core.config.AppConfig;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Demo: Unit test for Echo services
 * @author CH176656
 *
 */
public class EchoTest {
    
    
    /**
     * Test base cases of the method
     * @throws Exception
     */
    @Test
    public void echoGetTestBaseCase() throws Exception {
        Echo echo = new Echo();
        
        // We test empty string
        String send = "";
        Response resp = echo.getEcho(send, null);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        Echo.ReturnDTO ret = (Echo.ReturnDTO) resp.getEntity();
        assertEquals("Echo: ",ret.getVar());
        
        // We test non-empty string
        send = "ping";
        resp = echo.getEcho(send, null);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        ret = (Echo.ReturnDTO) resp.getEntity();
        assertEquals("Echo: " + send, ret.getVar());
        
        //Test bad request
        resp = echo.getEcho(null, null);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
    }

    @Ignore
    public void putMedicationsByPassIT() throws Exception {
        String patientId = "999999999";
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
            encounterNum = rs.getString("encounter_num");
            String modifierCd =  rs.getString("modifier_cd");
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
