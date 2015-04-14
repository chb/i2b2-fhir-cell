package org.bch.i2me2.core.external;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.util.HttpRequest;
import org.bch.i2me2.core.util.Response;
import org.bch.i2me2.core.util.Utils;
import org.bch.i2me2.core.util.Validator;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by CH176656 on 3/23/2015.
 */
public class RXConnect extends WrapperAPI {
    public static String PARAM_FIRST_NAME="firstName";
    public static String PARAM_LAST_NAME="lastName";
    public static String PARAM_BIRTH_DATE="birthDate";
    public static String PARAM_GENDER="sex";
    public static String PARAM_ZIP_CODE="zipCode";
    private static String PARAM_BIRTH_OF_DATE_FORMAT = "yyyyMMdd";
    private static String PARAM_ZIP_CODE_REGEXP="^\\d{5}(?:[-\\s]\\d{4})?$";

    private static String MODULE = "[RXCONNECT]";
    private static String OP_GET_MEDS_LIST = "[GET_MEDICATIONS_LIST]";
    private static String OP_VALIDATE = "[VALIDATE]";

    private String firstName;
    private String lastName;
    private String birthDate;
    private String gender;
    private String zipCode;

    /**
     * Return the json-formatted String of medications provided by RXConnect
     * @return The json-formatted string
     * @throws I2ME2Exception If param validation fails or http response code is higher than 400
     * @throws IOException If connection errors are produced
     */
    public String getMedicationsList() throws I2ME2Exception, IOException {
        validate();
        String url = generateURL();
        String auth = null;
        try {
            String cred = AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_FILE_RXCONNECT);
            javax.xml.bind.DatatypeConverter.printBase64Binary(cred.getBytes());
            auth = "Basic " + new String(javax.xml.bind.DatatypeConverter.printBase64Binary(cred.getBytes()));
        } catch (IOException e) {
            this.log(Level.WARNING, MODULE+OP_GET_MEDS_LIST+
                    "No authentication credentials found for rxconnect. Trying without authentication");
        }
        System.out.println(url);
        System.out.println(auth);
        Response resp = getHttpRequest().doPostGeneric(url, null, auth, null, "GET");
        if (resp.getResponseCode()>= 400) {
            this.log(Level.SEVERE, MODULE+OP_GET_MEDS_LIST+"RXConnect error. Error code: " + resp.getResponseCode());
            throw new I2ME2Exception("RXConnect error. Error code: " + resp.getResponseCode());
        }
        return resp.getContent();
    }


    public String getMedicationsList(String firstName, String lastName,
                                     String zipCode, String birthDate,
                                     String gender) throws I2ME2Exception, IOException {
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setBirthDate(birthDate);
        this.setZipCode(zipCode);
        this.setGender(gender);
        return this.getMedicationsList();
    }

    private String generateURL() throws I2ME2Exception {
        String baseUrl = Utils.generateURL(
                AppConfig.getProp(AppConfig.NET_PROTOCOL_RXCONNECT),
                AppConfig.getProp(AppConfig.HOST_RXCONNECT),
                AppConfig.getProp(AppConfig.PORT_RXCONNECT),
                AppConfig.getProp(AppConfig.EP_RXCONNECT));

        StringBuffer sb = new StringBuffer(baseUrl);
        sb.append("?");
        sb.append(HttpRequest.urlParam(PARAM_FIRST_NAME, this.firstName));
        sb.append("&");
        sb.append(HttpRequest.urlParam(PARAM_LAST_NAME, this.lastName));
        sb.append("&");
        sb.append(HttpRequest.urlParam(PARAM_BIRTH_DATE, this.birthDate));
        sb.append("&");
        sb.append(HttpRequest.urlParam(PARAM_GENDER, this.gender));
        sb.append("&");
        sb.append(HttpRequest.urlParam(PARAM_ZIP_CODE, this.zipCode));
        return sb.toString();
    }


    private void validate() throws I2ME2Exception {
        try {
            Validator.NotNullEmptyStr(this.firstName, "RX " + PARAM_FIRST_NAME);
            Validator.NotNullEmptyStr(this.lastName, "RX " + PARAM_LAST_NAME);

            Validator.validDate(this.birthDate, PARAM_BIRTH_OF_DATE_FORMAT, "RX " + PARAM_BIRTH_DATE);

            List<String> opt = new ArrayList<>();
            opt.add("M");
            opt.add("F");
            Validator.validOptions(this.gender, opt, "RX " + PARAM_GENDER);
            Validator.validRegExp(this.zipCode, PARAM_ZIP_CODE_REGEXP, "RX " + PARAM_ZIP_CODE);
        } catch (I2ME2Exception e) {
            this.log(Level.SEVERE, MODULE+OP_VALIDATE+e.getMessage());
            throw e;
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

}
