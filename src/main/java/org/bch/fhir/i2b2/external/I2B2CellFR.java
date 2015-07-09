package org.bch.fhir.i2b2.external;

import org.apache.commons.codec.binary.Hex;
import org.bch.fhir.i2b2.config.AppConfig;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.bch.fhir.i2b2.exception.I2ME2Exception;
import org.bch.fhir.i2b2.util.Response;
import org.bch.fhir.i2b2.util.Utils;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.bch.fhir.i2b2.util.mapper.Mapper;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Created by CH176656 on 3/25/2015.
 */
@Stateless
public class I2B2CellFR extends WrapperAPI {

    private static StringBuffer sendTemplate = new StringBuffer();
    private static StringBuffer uploadTemplate = new StringBuffer();

    private static String MODULE = "[I2B2CELLFR]";
    private static String OP_SEND_FILE = "[SEND_FILE]";
    private static String OP_UP_FILE = "[UPLOAD_FILE]";
    private static String OP_PUSH_PDO = "[PUSH_PDOXML]";

    /**
     * Send a pdoxml file to i2b2 and uploads its content to the CRC cell
     * @param pdoxml            The pdo xml
     * @return                  The response of the i2b2
     * @throws I2ME2Exception
     * @throws IOException
     */
    public UploadI2B2Response pushPDOXML(String pdoxml) throws FHIRI2B2Exception, IOException {
        this.log(Level.INFO, MODULE+OP_PUSH_PDO+"IN");
        try {
            loadTemplates();
        } catch (IOException e) {
            this.log(Level.SEVERE, MODULE+OP_PUSH_PDO+"Error loading templates");
            throw e;
        }
        String fileName = sendFile(pdoxml);
        this.log(Level.INFO, MODULE+OP_PUSH_PDO+"File:"+fileName+ " sent to i2b2");
        UploadI2B2Response response = uploadFile(fileName);
        this.log(Level.INFO, MODULE+OP_PUSH_PDO+"File:"+fileName+ " uploaded to i2b2");
        return response;
    }

    private String sendFile(String pdoxml) throws FHIRI2B2Exception, IOException {
        // Generate the file name
        String fileName = generateFileName();

        // Get the credentials to access i2b2
        String credentials=null;
        try {
            credentials = AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_FILE_I2B2);
        } catch (IOException e) {
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

        // Generate body soap request
        String i2b2Message = generateFileSendRequest(
                dateTime,
                AppConfig.getProp(AppConfig.I2B2_DOMAIN),
                i2b2user,
                i2b2pwd,
                AppConfig.getProp(AppConfig.I2B2_PROJECT_ID),
                calcFileSize(pdoxml),
                fileName,
                calcFileHash(pdoxml));

        // Generate the url
        String url = generateURLSend();

        // Send the SOAP message
        Response response = getSoapRequest().sendSoap(
                url,
                i2b2Message,
                AppConfig.getProp(AppConfig.SOAP_ACTION_I2B2_FR_SEND),
                fileName,
                pdoxml);

        //System.out.println("STATUS CODE SEND FILE:" + response.getResponseCode());
        if (response.getResponseCode()>=400) {
            this.log(Level.SEVERE, MODULE+OP_SEND_FILE+"I2B2 FR Send File Error.");
            throw new FHIRI2B2Exception("I2B2 FR Send File Error");
        }

        return fileName;
    }

    public String generateURLSend() throws FHIRI2B2Exception {
        return Utils.generateURL(
                AppConfig.getProp(AppConfig.NET_PROTOCOL_I2B2_FR),
                AppConfig.getProp(AppConfig.HOST_I2B2_FR),
                AppConfig.getProp(AppConfig.PORT_I2B2_FR),
                AppConfig.getProp(AppConfig.EP_I2B2_FR_SEND));
    }

    public String generateURLUpload() throws FHIRI2B2Exception {
        return Utils.generateURL(
                AppConfig.getProp(AppConfig.NET_PROTOCOL_I2B2_FR),
                AppConfig.getProp(AppConfig.HOST_I2B2_FR),
                AppConfig.getProp(AppConfig.PORT_I2B2_FR),
                AppConfig.getProp(AppConfig.EP_I2B2_CRC_UPLOAD));
    }

    private String generateFileName() {
        String filename= UUID.randomUUID().toString();
        //String filename= "test";
        return filename+".xml";
    }

    private UploadI2B2Response uploadFile(String fileName) throws FHIRI2B2Exception, IOException {
        this.log(Level.INFO, MODULE+OP_UP_FILE + "IN");
        // Get credentials
        String credentials = AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_FILE_I2B2);
        String i2b2user = "";
        String i2b2pwd = "";
        if (credentials != null) {
            String[] usrpwd = credentials.split(":");
            i2b2user = usrpwd[0];
            if (usrpwd.length > 1) {
                i2b2pwd = usrpwd[1];
            }
        }

        // Prepare date
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat(AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2));
        String dateTime = dateFormatOutput.format(new Date());

        // Prepare fullPath parameter
        String i2b2FileLocation = AppConfig.getProp(AppConfig.I2B2_FR_FILE_LOCATION);
        String fullPath = i2b2FileLocation + AppConfig.getProp(AppConfig.I2B2_PROJECT_ID) + "/" + fileName;

        // Generate the url
        String url = generateURLUpload();

        // Generate the body message
        String i2b2Message = generateFileUploadRequest(
                dateTime,
                AppConfig.getProp(AppConfig.I2B2_DOMAIN),
                i2b2user,
                i2b2pwd,
                AppConfig.getProp(AppConfig.I2B2_PROJECT_ID),
                fullPath,
                fileName);
        // Get content type for http request
        String contentType = AppConfig.getProp(AppConfig.REST_CONTENT_TYPE_I2B2_CRC_UPLOAD);

        // Do POST REST call
        Response response = getHttpRequest().doPostGeneric(url, i2b2Message, null, contentType);
        //Response response = getHttpRequest().doPostGeneric(url, i2b2Message, contentType);

        if (response.getResponseCode() >= 400) {
            this.log(Level.SEVERE, MODULE+OP_UP_FILE+ "Error uploading I2B2 File: " + fileName);
            throw new FHIRI2B2Exception("Error uploading I2B2 File: " + fileName);
        }
        UploadI2B2Response out=null;
        try {
            out = new UploadI2B2Response(response.getContent());
        } catch (Exception e) {
            this.log(Level.SEVERE, MODULE+OP_UP_FILE+ "Error parsing xml file from I2B2 response. File: " + fileName);
            throw new FHIRI2B2Exception("Error parsing xml file from I2B2.", e);

        }
        return out;
    }

    public static String generateFileSendRequest(String date, String i2b2Domain, String i2b2User,
                                                 String i2b2Pass, String projectId, String fileSize,
                                                 String fileName, String fileHash) {
        return String.format(sendTemplate.toString(),
                date, i2b2Domain, i2b2User, i2b2Pass, projectId, fileSize, fileName, fileHash, date);
    }


    private static String generateFileUploadRequest(String i2b2date, String i2b2Domain, String i2b2User,
                                                String i2b2Pass, String projectId, String fullFilePath,
                                                String fileName) {
        return String.format(uploadTemplate.toString(),
                i2b2date, i2b2Domain, i2b2User, i2b2Pass, projectId, fullFilePath, fileName);
    }

    private static void loadTemplates() throws IOException, FHIRI2B2Exception {
        if (sendTemplate.length()==0) {
            Utils.textFileToStringBuffer(
                    I2B2CellFR.class,
                    AppConfig.getProp(AppConfig.FIELNAME_SOAP_TEMP_I2B2_FR_SEND),
                    sendTemplate, "\n");
        }
        if (uploadTemplate.length()==0) {
            Utils.textFileToStringBuffer(
                    I2B2CellFR.class,
                    AppConfig.getProp(AppConfig.FIELNAME_SOAP_TEMP_I2B2_FR_UPLOAD),
                    uploadTemplate, "\n");
        }
    }

    public static class UploadI2B2Response {
        private static String TOTAL_RECORDS = "total_record";
        private static String IGNORED_RECORDS = "ignored_record";
        private static String INSERTED_RECORDS = "inserted_record";
        private Document doc;

        private String xmlResponse;

        public UploadI2B2Response(String xmlResponse) throws Exception {
            this.xmlResponse=xmlResponse;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlResponse));
            this.doc = dBuilder.parse(is);
        }

        public int getTotalRecords(Mapper.XmlPdoTag xmlTag) {
            return getValueFromNode(xmlTag, TOTAL_RECORDS);
        }

        public int getIgnoredRecords(Mapper.XmlPdoTag xmlTag) {
            return getValueFromNode(xmlTag, IGNORED_RECORDS);
        }

        public int getInsertedRecords(Mapper.XmlPdoTag xmlTag) {
            return getValueFromNode(xmlTag, INSERTED_RECORDS);
        }

        private int getValueFromNode(Mapper.XmlPdoTag xmlTag, String att) {
            Element element = getElement(xmlTag);
            if (element==null) return -1;
            String totalStr = element.getAttribute(att);
            return Integer.parseInt(totalStr);
        }

        private Element getElement(Mapper.XmlPdoTag xmlTag) {
            NodeList nodeList = this.doc.getElementsByTagName(xmlTag.toStringAlter());
            if (nodeList == null) return null;
            if (nodeList.getLength() > 1) return null;
            Node node = nodeList.item(0);
            return (Element) node;
        }

    }

    public static String calcFileHash(String fileData) {
        MessageDigest md;
        String hash = null;
        try {
            md = MessageDigest.getInstance("MD5");
            hash = new String(Hex.encodeHex(md.digest(fileData.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            // we will never be here because MD5 exists...
        }

        return hash;
    }

    public static String calcFileSize(String fileData) {
        if (fileData == null || fileData.isEmpty()) {
            return "0";
        }
        return BigInteger.valueOf(fileData.length()).toString();
    }

}
