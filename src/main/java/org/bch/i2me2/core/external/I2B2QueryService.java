package org.bch.i2me2.core.external;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.util.Response;
import org.bch.i2me2.core.util.Utils;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.w3c.dom.Document;

/**
 * Created by CH176656 on 4/7/2015.
 */
public class I2B2QueryService extends WrapperAPI {

    private static StringBuffer queryPdoTemplate = new StringBuffer();

    private static String MODULE = "[I2B2QUERYSERVICE]";
    private static String OP_GET_PATIENT_DATA = "[GET_PATIENT_DATA]";

    public QueryResponse getPatientData(String patientId, String source, Date date) throws IOException, I2ME2Exception {
        this.log(Level.INFO, MODULE+OP_GET_PATIENT_DATA+":IN, patientID:" + patientId);
        try {
            loadTemplateQueryPdo();
        } catch (IOException e) {
            this.log(Level.SEVERE, MODULE+OP_GET_PATIENT_DATA+"Error loading templates");
            throw e;
        }

        // Get the credentials to access i2b2
        String credentials=null;
        try {
            credentials = AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_FILE_I2B2);
        } catch (IOException e) {
            this.log(Level.WARNING, MODULE+OP_GET_PATIENT_DATA+"I2B2 credentials not found. Continue without");
            // It means the file does not exists
        }
        String i2b2user="";
        String i2b2pwd="";
        if (credentials!=null) {
            String[] usrpwd = credentials.split(":");
            i2b2user = usrpwd[0];
            if (usrpwd.length > 1) {
                i2b2pwd = usrpwd[1];
            }
        }
        // Prepare date
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat(AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2));
        String dateTime = dateFormatOutput.format(new Date());
        String dateTimeFrom = dateFormatOutput.format(date);

        // Generate the url
        String url = generateURLQuery();

        // Generate the body message
        String i2b2Message = generateQueryPdoRequest(
                dateTime,
                AppConfig.getProp(AppConfig.I2B2_DOMAIN),
                i2b2user,
                i2b2pwd,
                AppConfig.getProp(AppConfig.I2B2_PROJECT_ID),
                source,
                patientId,
                dateTimeFrom);

        // Get content type for http request
        String contentType = AppConfig.getProp(AppConfig.REST_CONTENT_TYPE_I2B2_CRC_QUERY);
        System.out.println("Content-Type: " + contentType);
        System.out.println("URL: " + url);
        System.out.println(i2b2Message);
        // Do POST REST call
        Response response = getHttpRequest().doPostGeneric(url, i2b2Message, null, null, "PUT");
        //Response response = getHttpRequest().doPostGeneric(url+"/"+ URLEncoder.encode("i2b2Message", "UTF-8"), null, null, null);

        if (response.getResponseCode() >= 400) {
            this.log(Level.SEVERE, MODULE+OP_GET_PATIENT_DATA+ "Error querying i2b2. patientId: " + patientId + ", " +
                    "patientIdSource:" + source);
            throw new I2ME2Exception("Error querying i2b2. patientId: " + patientId + ", "+
                    "patientIdSource:" + source);
        }

        QueryResponse out=null;
        try {
            out = new QueryResponse(response.getContent());
        } catch (Exception e) {
            this.log(Level.SEVERE, MODULE+OP_GET_PATIENT_DATA+ "Error parsing xml response from I2B2." +
                    patientId + ", " + "patientIdSource:" + source );
            throw new I2ME2Exception("Error parsing xml file from I2B2.", e);

        }
        return out;
    }

    private static String generateURLQuery() throws I2ME2Exception{
        return Utils.generateURL(
                AppConfig.getProp(AppConfig.NET_PROTOCOL_I2B2_CRC),
                AppConfig.getProp(AppConfig.HOST_I2B2_CRC),
                AppConfig.getProp(AppConfig.PORT_I2B2_CRC),
                AppConfig.getProp(AppConfig.EP_I2B2_CRC_PDOREQUEST));
    }

    private static String generateQueryPdoRequest(String dateMsg, String domain, String user, String password,
                                                  String projectId, String patientId, String patientIdSource,
                                                  String dateFrom) {
        //return queryPdoTemplate.toString();
        return String.format(queryPdoTemplate.toString(),
                dateMsg, domain, user, password, projectId, patientId, patientIdSource, dateFrom);

    }

    private static void loadTemplateQueryPdo() throws IOException, I2ME2Exception{
        if (queryPdoTemplate.length()==0) {
            Utils.textFileToStringBuffer(
                    I2B2QueryService.class,
                    AppConfig.getProp(AppConfig.FILENAME_REST_TEMP_I2B2_CRC_QUERYPDO),
                    queryPdoTemplate, "\n");
        }
    }

    public static class QueryResponse {

        private String xmlResponse;
        private Document doc;

        public QueryResponse(String xmlResponse) throws Exception {
            this.xmlResponse=xmlResponse;
            System.out.println(this.xmlResponse);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlResponse));
            this.doc = dBuilder.parse(is);
        }

    }
}
