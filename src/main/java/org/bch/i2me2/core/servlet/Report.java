package org.bch.i2me2.core.servlet;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CH176656 on 5/26/2015.
 */
public class Report extends HttpServlet {
    private static final String F_SOURCE = "Source";
    private static final String F_MEDICATION = "Medication Code";
    private static final String F_MEDICATION_NAME = "Medication Name";
    private static final String F_STATUS = "Status";
    private static final String F_DATE = "Start date";

    private static final String SEP = ",";

    private static final List<String> orderList = new ArrayList<>();
    private static final Map<String, String> modifierMap = new HashMap();
    private static final List<String> modifierList = new ArrayList();

    private static final String SELECT_OBS_FACT_BASE =
            "Select distinct concept_cd, start_date, modifier_cd, tval_char, nval_num, instance_num, valtype_cd " +
            "from observation_fact ob, encounter_mapping em " +
            "where ob.encounter_num=em.encounter_num and em.encounter_ide_source='%s' and " +
            "patient_num = %s";

    private static final String ORDER_BY_DATE = " order by start_date";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        initRows();
        String subjectId = request.getParameter("subjectId");
        String filename = generateCSVFile(subjectId);
        File file = new File(filename);
        if(!file.exists()){
            throw new ServletException("File doesn't exists on server.");
        }
        //System.out.println("File location on server::"+file.getAbsolutePath());
        ServletContext ctx = getServletContext();
        InputStream fis = new FileInputStream(file);
        String mimeType = ctx.getMimeType(file.getAbsolutePath());
        response.setContentType(mimeType != null? mimeType:"application/octet-stream");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        ServletOutputStream os = response.getOutputStream();
        byte[] bufferData = new byte[1024];
        int read=0;
        while((read = fis.read(bufferData))!= -1){
            os.write(bufferData, 0, read);
        }
        os.flush();
        os.close();
        fis.close();
    }

    private void initRows() {
        // Indicates the order in which the columns will appear
        this.orderList.add(F_SOURCE);
        this.orderList.add(F_MEDICATION);
        this.orderList.add(F_MEDICATION_NAME);
        this.orderList.add(F_DATE);
        this.orderList.add(F_STATUS);

        // Maps the columns with the corresponding internal modifier code. In the case of multiple modifieres for the
        // same column the program will explore all of them to see if some matches.
        // Columns F_SOURCE, F_MEDICATION and F_DATE do not come from modifiers
        this.modifierMap.put(F_MEDICATION_NAME, "medicationNameStatement###medicationNameClaim###medicationName");
        this.modifierMap.put(F_STATUS, "statusStatement");

        // The complete list of modifiers that can be part of the columns list
        this.modifierList.add("medicationNameStatement");
        this.modifierList.add("medicationNameClaim");
        this.modifierList.add("medicationName");
        this.modifierList.add("statusStatement");


    }

    private String getHeaders() {
        String header="";
        for(String col: this.orderList) {
            if (!header.isEmpty()) header = header + SEP;
            header = header + col;
        }
        return header+"\n";
    }

    private String addElement(Map<String, Map<String, String>> map, String medication, String date, String rawModifier, String value) {
        String modifierInternal = AppConfig.getRealModifiersReverseMap().get(rawModifier);
        if (this.modifierList.contains(modifierInternal)) {
            String key = medication + "###" + date;
            String retKey = null;
            String currValue = value;
            Map<String, String> auxMap;
            if (map.containsKey(key)) {
                auxMap = map.get(key);
            } else {
                auxMap = new HashMap<>();
                map.put(key, auxMap);
                retKey=key;
            }
            if (auxMap.containsKey(modifierInternal)) {
                currValue = currValue + " - " + auxMap.get(key);
            }
            auxMap.put(modifierInternal, currValue);
            return retKey;
        }
        return null;
    }

    protected String generateCSVFile(String subjectId) throws IOException {
        String filename=null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            String jdbcCon = AppConfig.getProp(AppConfig.I2B2_JDBC);
            String auth = AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_DB_I2B2);
            String[] auths = auth.split(":");
            Connection con = DriverManager.getConnection(jdbcCon, auths[0], auths[1]);
            String patientNum = getPatientNum(con, subjectId);
            filename = subjectId+".csv";

            String idbData=null;
            if (subjectId.equals("98989898")) {
                idbData = getIBDData(con, "0010");
            } else {
                idbData = getIBDData(con, patientNum);
            }

            String sureScriptsData = getSureScriptsData(con, patientNum);
            String medrecData = getMedRecData(con, patientNum);
            String headers = getHeaders();
            PrintWriter out = new PrintWriter(filename);
            out.print(headers);
            out.print(idbData);
            out.print(sureScriptsData);
            out.print(medrecData);
            out.close();
            return filename;
        } catch (FileNotFoundException e) {
            throw new IOException("Error creating file " +  filename);
        } catch (ClassNotFoundException e) {
            throw new IOException("Error creating file " +  filename);
        } catch (I2ME2Exception e) {
            throw new IOException("Error creating file " +  filename);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOException("Error creating file " +  filename);
        }

    }

    private String getIBDData(Connection con, String patientNum) throws SQLException, I2ME2Exception {
        return getData(con, patientNum, AppConfig.getProp(AppConfig.I2B2_PDO_SOURCE_IBD));
    }

    private String getSureScriptsData(Connection con, String patientNum) throws SQLException, I2ME2Exception {
        return getData(con, patientNum, AppConfig.getProp(AppConfig.I2B2_PDO_SOURCE_SURESCRIPT));
    }

    private String getMedRecData(Connection con, String patientNum) throws SQLException, I2ME2Exception {
        return getData(con, patientNum, AppConfig.getProp(AppConfig.I2B2_PDO_SOURCE_BCH));
    }


    private String getData(Connection con, String patientNum, String source) throws SQLException, I2ME2Exception {
        String query = String.format(
                SELECT_OBS_FACT_BASE,
                source,
                patientNum) + ORDER_BY_DATE;

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        Map<String, Map<String, String>> mapData = new HashMap<>();
        List<String> keys = new ArrayList<>();
        while(rs.next()) {
            String conceptCd = rs.getString("concept_cd");
            String startDate = rs.getString("start_date");
            String modifierCd = rs.getString("modifier_cd");
            String tvalChar = rs.getString("tval_char");
            String nvalNum = rs.getString("nval_num");
            String valtypeCd = rs.getString("valtype_cd");

            String value = tvalChar;
            if (valtypeCd!=null) {
                if (valtypeCd.toLowerCase().equals("N")) {
                    value = nvalNum;
                }
            }
            String key = addElement(mapData, conceptCd, startDate, modifierCd, value);
            if (key!=null) {
                keys.add(key);
            }
        }
        rs.close();

        return generateRows(mapData, keys, source);

    }

    private String generateRows(Map<String, Map<String, String>> mapData, List<String> keys, String source) {
        String finalRows="";
        for(String key:keys) {
            if (mapData.containsKey(key)) {
                Map<String, String> auxMap = mapData.get(key);
                String []datas = key.split("###");
                String row = generateRow(datas[0], datas[1], auxMap, source);
                finalRows = finalRows + row + "\n";
            }
        }
        return finalRows;
    }

    String generateRow(String medication, String startDate, Map<String,String> auxMap, String source) {
        String row="";
        for(String elem: this.orderList) {
            if (!row.isEmpty()) row = row + ",";
            if (elem.equals(F_SOURCE)) {
                row = row + source;
            } else if (elem.equals(F_MEDICATION)) {
                row = row + medication;
            } else if (elem.equals(F_DATE)) {
                row = row + startDate;
            } else {
                String keyModif = this.modifierMap.get(elem);
                String []modifs = keyModif.split("###");
                for (String mod: modifs) {
                    if (auxMap.containsKey(mod)) {
                        row = row + auxMap.get(mod);
                    }
                }
            }
        }
        return row;
    }

    private String getPatientNum(Connection con, String subjectId) throws SQLException, I2ME2Exception {
        String query = "Select patient_num from patient_mapping where patient_ide='" + subjectId + "' " +
                "and patient_ide_source='" + AppConfig.getProp(AppConfig.I2B2_PDO_SOURCE_BCH) + "'";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        // It must be one entry
        rs.next();
        String patientNum = rs.getString("patient_num");
        return patientNum;
    }
}
