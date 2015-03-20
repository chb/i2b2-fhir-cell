package org.bch.i2me2.core.config;

import org.bch.i2me2.core.exception.I2ME2Exception;

import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

/**
 * Configuration file
 * Created by CH176656 on 3/20/2015.
 */
public class AppConfig {

    // The keys of the configuration parameters
    public static String URL_RXCONNECT = "app.url.rxconnect";
    public static String URL_IDM = "app.url.idm";
    public static String URL_I2B2_CRC = "app.url.i2b2.crc";

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
}
