package org.bch.i2me2.core.external;

import org.apache.commons.codec.binary.Hex;
import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
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
public class I2B2CellFR {

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
        String fileName = sendFile(pdoxml);
        uploadFile(fileName);
    }

    private String sendFile(String pdoxml) throws I2ME2Exception, IOException {
        String fileName = generateFileName();
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

        SimpleDateFormat dateFormatOutput = new SimpleDateFormat(AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2));
        String dateTime = dateFormatOutput.format(new Date());
        String i2b2Message = generateFileSendRequest(
                dateTime,
                AppConfig.getProp(AppConfig.I2B2_DOMAIN),
                i2b2user,
                i2b2pwd,
                AppConfig.getProp(AppConfig.I2B2_PROJECT_ID),
                calcFileSize(pdoxml),
                fileName,
                calcFileHash(pdoxml));

        String url = generateURLSend();
        Response response = SoapRequest.sendSoap(
                url,
                i2b2Message,
                AppConfig.getProp(AppConfig.SOAP_ACTION_I2B2_FR_SEND),
                fileName,
                pdoxml);
        if (response.getResponseCode()>400) {
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

    private String generateFileName() {
        String filename= UUID.randomUUID().toString();
        return filename+".xml";
    }

    private void uploadFile(String fileName) throws I2ME2Exception, IOException {
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
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat(AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2));
        String dateTime = dateFormatOutput.format(new Date());
        String i2b2FileLocation = AppConfig.getProp(AppConfig.I2B2_FR_FILE_LOCATION);
        String fullPath = i2b2FileLocation + AppConfig.getProp(AppConfig.I2B2_PROJECT_ID) + "/" + fileName;
        String i2b2Message = generateFileUploadRequest(
                dateTime,
                AppConfig.getProp(AppConfig.I2B2_DOMAIN),
                i2b2user,
                i2b2pwd,
                AppConfig.getProp(AppConfig.I2B2_PROJECT_ID),
                fullPath,
                fileName);

        // TODO: MAKE A POST REST
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
