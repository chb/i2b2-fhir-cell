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

/**
 * Created by CH176656 on 5/26/2015.
 */
public class Report extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

            PrintWriter out = new PrintWriter(filename);
            out.print("1,2,3,4,5,6,7\n7,6,5,4,3,2,1");
            out.close();
            return filename;
        } catch (FileNotFoundException e) {
            throw new IOException("Error creating file " +  filename);
        } catch (ClassNotFoundException e) {
            throw new IOException("Error creating file " +  filename);
        } catch (I2ME2Exception e) {
            throw new IOException("Error creating file " +  filename);
        } catch (SQLException e) {
            throw new IOException("Error creating file " +  filename);
        }

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
