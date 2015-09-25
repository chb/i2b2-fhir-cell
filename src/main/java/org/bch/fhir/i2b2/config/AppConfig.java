package org.bch.fhir.i2b2.config;

import org.apache.commons.io.IOUtils;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.bch.fhir.i2b2.exception.I2ME2Exception;
import org.bch.fhir.i2b2.service.WrapperService;
import org.bch.fhir.i2b2.util.Utils;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

/**
 * Configuration file
 * Created by CH176656 on 3/20/2015.
 */
public class AppConfig {

    // The keys of the configuration parameters

    // The end points
    public static String EP_I2B2_FR_SEND =          "app.endpoint.i2b2.fr.send";
    public static String EP_I2B2_CRC_UPLOAD =       "app.endpoint.i2b2.crc.upload";
    public static String EP_I2B2_CRC_PDOREQUEST =   "app.endpoint.i2b2.crc.pdorequest";

    // The hosts
    public static String HOST_I2B2_FR =             "app.host.i2b2.fr";
    public static String HOST_I2B2_CRC =             "app.host.i2b2.crc";

    // The ports
    public static String PORT_I2B2_FR =             "app.port.i2b2.fr";
    public static String PORT_I2B2_CRC =             "app.port.i2b2.crc";

    // The internet protocol (http|https)
    public static String NET_PROTOCOL_I2B2_FR =     "app.network.protocol.i2b2.fr";
    public static String NET_PROTOCOL_I2B2_CRC =     "app.network.protocol.i2b2.crc";

    // The location file of the credentials
    public static String CREDENTIALS_FILE_I2B2 =        "app.authfile.i2b2.fr";
    //public static String CREDENTIALS_FILE_DB_I2B2 =     "app.authfile.db.i2b2";

    // The message templates for i2b2 messages
    public static String FIELNAME_SOAP_TEMP_I2B2_FR_SEND =  "app.filename.soap.template.i2b2.fr.send";
    public static String FIELNAME_SOAP_TEMP_I2B2_FR_UPLOAD ="app.filename.soap.template.i2b2.fr.upload";
    public static String FILENAME_REST_TEMP_I2B2_CRC_QUERYPDO ="app.filename.rest.template.i2b2.crc.querypdo";

    // The SOAP action for sending files to I2B2 FR
    public static String SOAP_ACTION_I2B2_FR_SEND =         "app.soap.action.i2b2.fr.send";

    // The I2B2 format string for dates
    public static String FORMAT_DATE_I2B2 =                 "app.i2b2.format.date";

    // The I2B2 project id, domain and file location. These parameters are placed in the i2b2 messages
    public static String I2B2_PROJECT_ID =                  "app.ib2b.projectid";
    public static String I2B2_DOMAIN =                      "app.ib2b.domain";
    public static String I2B2_FR_FILE_LOCATION =            "app.i2b2.fr.file.location";

    // The content-type of the rest requests
    public static String REST_CONTENT_TYPE_I2B2_CRC_UPLOAD=  "app.rest.contenttype.i2b2.crc.upload";
    public static String REST_CONTENT_TYPE_I2B2_CRC_QUERY=  "app.rest.contenttype.i2b2.crc.query";

    // The patient and encounter source when data comming from surescripts and from bch
    public static String I2B2_PDO_SOURCE_BCH =              "app.i2b2.pdo.source.bch";

    //public static String I2B2_JDBC =                        "app.jdbc.i2b2";
    public static final String I2B2_DATASOURCE =            "app.datasource.i2b2";

    // Other constants
    public static int HTTP_TRANSPORT_BUFFER_SIZE = 500;
    public static String CONFIG_PROPERTIES_FILE= "config.properties";

    private static Properties prop = new Properties();

    private static final Map<String, String> realModifiers = new HashMap<>();
    private static final Map<String, String> realModifiersReverse = new HashMap<>();

    private static final Map<String, String> realConceptCodes = new HashMap<>();
    private static final Map<String, String> conceptCodesType = new HashMap<>();

    private static final Map<String, String> realConceptCodesObs = new HashMap<>();
    private static final Map<String, String> conceptCodesTypeObs = new HashMap<>();


    // File name containing the list of real modifier codes
    private static final String REAL_MODIFIERS_FILE = "modifierCodes.i2me2";

    private static final String REAL_CONCEPT_CODES_FILE = "conceptCodes.c3pro";
    private static final String CONCEPT_CODES_TYPE_FILE = "conceptCodesType.c3pro";
    private static final String REAL_CONCEPT_CODES_FILE_OBS = "observationCodes.c3pro";
    private static final String CONCEPT_CODES_TYPE_FILE_OBS = "observationCodesType.c3pro";

    // separator between concept codes and concept_cd and types for *.c3pro
    private static final String SEP = ":::";

    /**
     * Upload the configuration from config.properties files
     */
    private static void uploadConfiguration() throws FHIRI2B2Exception {
        InputStream input = null;

        try {
            String filename = CONFIG_PROPERTIES_FILE;
            input = AppConfig.class.getResourceAsStream(filename);
            if (input == null) {
                throw new FHIRI2B2Exception("No " + filename + " has found!");
            }
            prop.load(input);

        } catch (IOException ex) {
            throw new FHIRI2B2Exception("Properties file error", ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    throw new FHIRI2B2Exception("Error closing properties file", e);
                }
            }
        }
    }


    public static Map<String, String> getRealConceptCodesObsMap() {
        return getRealConceptCodesMapGeneric(REAL_CONCEPT_CODES_FILE_OBS, realConceptCodesObs);
    }

    public static Map<String, String> getRealConceptCodesTypeObsMap() {
        return getRealConceptCodesMapGeneric(CONCEPT_CODES_TYPE_FILE_OBS, conceptCodesTypeObs);
    }

    private static Map<String, String> getRealConceptCodesMapGeneric(String file, Map<String, String> out) {
        if (out.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            try {
                Utils.textFileToStringBuffer(WrapperService.class, file, sb, ",");
            } catch (Exception e) {
                return out;
            }
            String [] modifiers = sb.toString().split(",");
            for (String modifier: modifiers){
                if (modifier.contains(SEP)) {
                    String[] codes = modifier.split(SEP);
                    out.put(codes[0].trim(), codes[1].trim());
                }
            }
        }
        return out;
    }

    public static Map<String, String> getRealConceptCodesMap() {
        return getRealConceptCodesMapGeneric(REAL_CONCEPT_CODES_FILE, realConceptCodes);
    }

    public static Map<String, String> getConceptCodesTypeMap() {
        return getRealConceptCodesMapGeneric(CONCEPT_CODES_TYPE_FILE, conceptCodesType);
    }

    public static Map<String, String> getRealModifiersMap() {
        if (realModifiers.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            try {
                Utils.textFileToStringBuffer(WrapperService.class, REAL_MODIFIERS_FILE, sb, ",");
            } catch (Exception e) {
                return realModifiers;
            }
            String [] modifiers = sb.toString().split(",");
            for (String modifier: modifiers){
                String [] codes = modifier.split(":");
                realModifiers.put(codes[0].trim(), codes[1].trim());
            }
        }
        return realModifiers;
    }

    public static Map<String, String> getRealModifiersReverseMap() {
        if (realModifiersReverse.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            try {
                Utils.textFileToStringBuffer(WrapperService.class, REAL_MODIFIERS_FILE, sb, ",");
            } catch (Exception e) {
                return realModifiersReverse;
            }
            String [] modifiers = sb.toString().split(",");
            for (String modifier: modifiers){
                String [] codes = modifier.split(":");
                realModifiersReverse.put(codes[1].trim(), codes[0].trim());
            }
        }
        return realModifiersReverse;
    }

    public static String getProp(String key) throws FHIRI2B2Exception {
        if (prop.isEmpty()) {
            uploadConfiguration();
        }
        return prop.getProperty(key);
    }

    public static String getAuthCredentials(String key) throws IOException, FHIRI2B2Exception {
        String path = getProp(key);
        String finalPath = path;
        int i = path.indexOf("[");
        int j = path.indexOf("]");
        if (i<0 && j>=0) throw new FHIRI2B2Exception("Missing [ in " + key);
        if (i>=0) {
            if (j<0) throw new FHIRI2B2Exception("Missing ] in " + key);
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
