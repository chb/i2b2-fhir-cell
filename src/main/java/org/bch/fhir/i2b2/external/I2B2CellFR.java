package org.bch.fhir.i2b2.external;

import org.apache.commons.codec.binary.Hex;
import org.bch.fhir.i2b2.config.AppConfig;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
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

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Manages the connectivity between the I2B2 FR and CRC cell to push pdo xml files and upload them into i2b2 using
 * the standard i2b2 messaging
 * @author CHIP-IHL
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
     * @throws FHIRI2B2Exception
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

    /**
     * Generates the URL to connect to the I2B2 FR cell
     * @return The URL
     * @throws FHIRI2B2Exception In case there are missing properties that prevent to construct the URL
     */
    public String generateURLSend() throws FHIRI2B2Exception {
        return Utils.generateURL(
                AppConfig.getProp(AppConfig.NET_PROTOCOL_I2B2_FR),
                AppConfig.getProp(AppConfig.HOST_I2B2_FR),
                AppConfig.getProp(AppConfig.PORT_I2B2_FR),
                AppConfig.getProp(AppConfig.EP_I2B2_FR_SEND));
    }

    /**
     * Generates the URL where to connect to the I2B2 CRC cell
     * @return The URL
     * @throws FHIRI2B2Exception In case there are missing properties that prevent to construct the URL
     */

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

    /**
     * Generates the pdo xml message to I2B2 FR to send a file
     * @param date          The date
     * @param i2b2Domain    The I2B2 domain
     * @param i2b2User      The authorized I2B2 user to upload the file
     * @param i2b2Pass      The password
     * @param projectId     The I2B2 project id
     * @param fileSize      The file size calculated with {@link I2B2CellFR#calcFileSize(String)}
     * @param fileName      The filename
     * @param fileHash      The hash of the file calculated with {@link I2B2CellFR#calcFileHash(String)}
     * @return              The message
     */
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

    /**
     * Class to capture the responses from I2B2
     * @author CHIP-IHL
     */
    public static class UploadI2B2Response {
        private static String TOTAL_RECORDS = "total_record";
        private static String IGNORED_RECORDS = "ignored_record";
        private static String INSERTED_RECORDS = "inserted_record";
        private Document doc;

        private String xmlResponse;

        /**
         * Contructor
         * @param xmlResponse   The response from I2B2
         * @throws Exception    In case xmlResponse cannot be parsed
         */
        public UploadI2B2Response(String xmlResponse) throws Exception {
            this.xmlResponse=xmlResponse;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlResponse));
            this.doc = dBuilder.parse(is);
        }

        /**
         * Returns the total number of processed records tagged with xmlTag
         * @param xmlTag    The tag
         * @return          The number of elements with xmlTag
         */
        public int getTotalRecords(XmlPdoTag xmlTag) {
            return getValueFromNode(xmlTag, TOTAL_RECORDS);
        }

        /**
         * Returns the total number of ignored records tagged with xmlTag
         * @param xmlTag    The tag
         * @return          The number of elements with xmlTag
         */
        public int getIgnoredRecords(XmlPdoTag xmlTag) {
            return getValueFromNode(xmlTag, IGNORED_RECORDS);
        }

        /**
         * Returns the total number of inserted records tagged with xmlTag
         * @param xmlTag    The tag
         * @return          The number of elements with xmlTag
         */
        public int getInsertedRecords(XmlPdoTag xmlTag) {
            return getValueFromNode(xmlTag, INSERTED_RECORDS);
        }

        private int getValueFromNode(XmlPdoTag xmlTag, String att) {
            Element element = getElement(xmlTag);
            if (element==null) return -1;
            String totalStr = element.getAttribute(att);
            return Integer.parseInt(totalStr);
        }

        private Element getElement(XmlPdoTag xmlTag) {
            NodeList nodeList = this.doc.getElementsByTagName(xmlTag.toStringAlter());
            if (nodeList == null) return null;
            if (nodeList.getLength() > 1) return null;
            Node node = nodeList.item(0);
            return (Element) node;
        }

    }

    /**
     * Computes the MD5 hash of the file
     * @param fileData  The file content
     * @return          The hash value
     */
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

    /**
     * Computes the size of the file
     * @param fileData  The file content
     * @return          The size in number of characters
     */
    public static String calcFileSize(String fileData) {
        if (fileData == null || fileData.isEmpty()) {
            return "0";
        }
        return BigInteger.valueOf(fileData.length()).toString();
    }

    /**
     * The enum class for the xml pdo set tags
     * @author CHIP-IHL
     *
     */
    public static enum XmlPdoTag {
        TAG_OBSERVATIONS ("observation_set", "observation_set", "observation"),
        TAG_EVENTS ("event_set", "event_set" , "event"),
        TAG_CONCEPTS ("concept_set", "concept_set", "concept"),
        TAG_EIDS ("eid_set","eventid_set", "eid"),
        TAG_PIDS ("pid_set","pid_set", "pid"),
        TAG_MODIFIERS ("modifier_set","modifier_set", "modifier"),
        TAG_PATIENTS ("patient_set","patient_set", "patient"),
        TAG_REPOSITORY ("repository:patient_data","repository:patient_data", "");

        private final String tagValue;
        private final String tagValueIn;
        private final String tagAlter;
        XmlPdoTag(String tagValue, String tagAlter, String tagValueIn) {
            this.tagValueIn = tagValueIn;
            this.tagValue = tagValue;
            this.tagAlter = tagAlter;
        }
        public String toString() {
            return this.tagValue;
        }
        public String toStringAlter() { return this.tagAlter; }
        public String getTagValueIn() {
            return this.tagValueIn;
        }
    }

}
