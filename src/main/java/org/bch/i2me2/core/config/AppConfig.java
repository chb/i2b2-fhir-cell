package org.bch.i2me2.core.config;

import org.apache.commons.io.IOUtils;
import org.bch.i2me2.core.exception.I2ME2Exception;

import java.io.FileInputStream;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

/**
 * Configuration file
 * Created by CH176656 on 3/20/2015.
 */
public class AppConfig {

    // The keys of the configuration parameters
    public static String EP_RXCONNECT =             "app.endpoint.rxconnect";
    public static String EP_IDM_RESOURCE =          "app.endpoint.idm.resource";
    public static String EP_IDM_ID =                "app.endpoint.idm.id";
    public static String EP_I2B2_FR_SEND =          "app.endpoint.i2b2.fr.send";
    public static String EP_I2B2_FR_UPLOAD =        "app.endpoint.i2b2.fr.upload";

    public static String HOST_RXCONNECT =           "app.host.rxconnect";
    public static String HOST_I2B2_FR =             "app.host.i2b2.fr";
    public static String HOST_IDM =                 "app.host.idm";

    public static String PORT_RXCONNECT =           "app.port.rxconnect";
    public static String PORT_IDM =                 "app.port.idm";
    public static String PORT_I2B2_FR =             "app.port.i2b2.fr";

    public static String NET_PROTOCOL_RXCONNECT =   "app.network.protocol.rxconnect";
    public static String NET_PROTOCOL_IDM =         "app.network.protocol.idm";
    public static String NET_PROTOCOL_I2B2_FR =     "app.network.protocol.i2b2.fr";


    public static String CREDENTIALS_FILE_RXCONNECT =   "app.authfile.rxconnect";
    public static String CREDENTIALS_FILE_IDM =         "app.authfile.idm";
    public static String CREDENTIALS_FILE_I2B2 =        "app.authfile.i2b2.fr";

    public static String FIELNAME_SOAP_TEMP_I2B2_FR_SEND =  "app.filename.soap.template.i2b2.fr.send";
    public static String FIELNAME_SOAP_TEMP_I2B2_FR_UPLOAD ="app.filename.soap.template.i2b2.fr.upload";

    public static String SOAP_ACTION_I2B2_FR_SEND =         "app.soap.action.i2b2.fr.send";
    public static String FORMAT_DATE_I2B2 =                 "app.i2b2.format.date";
    public static String I2B2_PROJECT_ID =                  "app.ib2b.projectid";
    public static String I2B2_DOMAIN =                      "app.ib2b.domain";
    public static String I2B2_FR_FILE_LOCATION =            "app.i2b2.fr.file.location";

    public static String REST_CONTENT_TYPE_I2B2_FR_UPLOAD=  "app.rest.contenttype.i2b2.fr.upload";

    // Other constants
    public static int HTTP_TRANSPORT_BUFFER_SIZE = 500;
    public static String CONFIG_PROPERTIES_FILE= "config.properties";

    private static Properties prop = new Properties();

    /**
     * Upload the configuration from config.properties files
     */
    private static void uploadConfiguration() throws I2ME2Exception {
        InputStream input = null;

        try {
            String filename = CONFIG_PROPERTIES_FILE;
            input = AppConfig.class.getResourceAsStream(filename);
            if (input == null) {
                throw new I2ME2Exception("No " + filename + " has found!");
            }
            prop.load(input);

        } catch (IOException ex) {
            throw new I2ME2Exception("Properties file error", ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    throw new I2ME2Exception("Error closing properties file", e);
                }
            }
        }
    }

    public static String getProp(String key) throws I2ME2Exception {
        if (prop.isEmpty()) {
            uploadConfiguration();
        }
        return prop.getProperty(key);
    }

    public static String getAuthCredentials(String key) throws IOException, I2ME2Exception {
        String path = getProp(key);
        String finalPath = path;
        int i = path.indexOf("[");
        int j = path.indexOf("]");
        if (i<0 && j>=0) throw new I2ME2Exception("Missing [ in " + key);
        if (i>=0) {
            if (j<0) throw new I2ME2Exception("Missing ] in " + key);
            String var = path.substring(i+1,j);
            String aux = System.getenv(var);
            if (aux == null) aux = "";
            finalPath = path.replaceAll("\\[" + var + "\\]", aux);
        }
        FileInputStream inputStream = new FileInputStream(finalPath);
        String out=null;
        try {
            out = IOUtils.toString(inputStream).trim();
        } finally {
            inputStream.close();
        }
        return out;
    }
}
