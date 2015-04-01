package org.bch.i2me2.core.external;

import org.apache.commons.codec.binary.Hex;
import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.util.HttpRequest;
import org.bch.i2me2.core.util.Response;
import org.bch.i2me2.core.util.SoapRequest;
import org.bch.i2me2.core.util.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by CH176656 on 3/25/2015.
 */
public class I2B2CellFR extends WrapperAPI {

    private static StringBuffer sendTemplate = new StringBuffer();
    private static StringBuffer uploadTemplate = new StringBuffer();

    /**
     * Send a pdoxml file to i2b2 and uploads its content to the CRC cell
     * @param pdoxml            The pdo xml
     * @throws I2ME2Exception
     * @throws IOException
     */
    public void pushPDOXML(String pdoxml) throws I2ME2Exception, IOException {
        loadTemplates();
        //String fileName = sendFile(pdoxml);
        uploadFile("test.xml");
        //uploadFile(fileName);
    }

    private String sendFile(String pdoxml) throws I2ME2Exception, IOException {
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
        System.out.println(url);
        System.out.println(i2b2Message);
        // Send the SOAP message
        Response response = getSoapRequest().sendSoap(
                url,
                i2b2Message,
                AppConfig.getProp(AppConfig.SOAP_ACTION_I2B2_FR_SEND),
                fileName,
                pdoxml);

        System.out.println("STATUS CODE SEND FILE:" + response.getResponseCode());
        if (response.getResponseCode()>=400) {
            throw new I2ME2Exception("I2B2 FR Send File Error");
        }

        return fileName;
    }

    public String generateURLSend() throws I2ME2Exception {
        return Utils.generateURL(
                AppConfig.getProp(AppConfig.NET_PROTOCOL_I2B2_FR),
                AppConfig.getProp(AppConfig.HOST_I2B2_FR),
                AppConfig.getProp(AppConfig.PORT_I2B2_FR),
                AppConfig.getProp(AppConfig.EP_I2B2_FR_SEND));
    }

    public String generateURLUpload() throws I2ME2Exception {
        return Utils.generateURL(
                AppConfig.getProp(AppConfig.NET_PROTOCOL_I2B2_FR),
                AppConfig.getProp(AppConfig.HOST_I2B2_FR),
                AppConfig.getProp(AppConfig.PORT_I2B2_FR),
                AppConfig.getProp(AppConfig.EP_I2B2_FR_UPLOAD));
    }

    private String generateFileName() {
        //String filename= UUID.randomUUID().toString();
        String filename= "test";
        return filename+".xml";
    }

    private void uploadFile(String fileName) throws I2ME2Exception, IOException {
        // Get credentials
        String credentials = AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_FILE_I2B2);
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

        System.out.println(i2b2Message);
        System.out.println(url);
        // Get content type for http request
        String contentType = AppConfig.getProp(AppConfig.REST_CONTENT_TYPE_I2B2_FR_UPLOAD);

        // Do POST REST call
        //Response response = getHttpRequest().doPostGeneric(url, i2b2Message, null, contentType);
        try {
            String out = getHttpRequest().sendRequest(i2b2Message, url);
            System.out.println(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        Response response=null;
        try {
            response = getSoapRequest().sendSoap(url, i2b2Message, null);
        } catch (Exception e) {
            // nothing
        }
*/
//        if (response.getResponseCode()>=400) {
 //           throw new I2ME2Exception("I2B2 FR Send File Error");
  //      }
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

    private static void loadTemplates() throws IOException, I2ME2Exception {
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

    public static String calcFileHash(String fileData) {
        MessageDigest md;
        String hash = null;
        try {
            md = MessageDigest.getInstance("MD5");
            hash = new String(Hex.encodeHex(md.digest(fileData.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            // we will will never be here because MD5 exists...
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
