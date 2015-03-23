package org.bch.i2me2.core.external;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.util.HttpRequest;
import org.bch.i2me2.core.util.Validator;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CH176656 on 3/23/2015.
 */
public class RXConnect {
    public static String PARAM_FIRST_NAME="firstName";
    public static String PARAM_LAST_NAME="lastName";
    public static String PARAM_BIRTH_DATE="birthDate";
    public static String PARAM_GENDER="sex";
    public static String PARAM_ZIP_CODE="zipCode";
    private static String PARAM_BIRTH_OF_DATE_FORMAT = "yyyyMMdd";
    private static String PARAM_ZIP_CODE_REGEXP="^\\d{5}(?:[-\\s]\\d{4})?$";

    private String firstName;
    private String lastName;
    private String birthDate;
    private String gender;
    private String zipCode;

    @Inject HttpRequest http;
    /**
     * Return the json-formatted String of medications provided by RXConnect
     * @return The json-formatted string
     * @throws I2ME2Exception If param validation fails or http response code is higher than 400
     * @throws IOException If connection errors are produced
     */
    public String getMedicationLists() throws I2ME2Exception, IOException {

        validate();
        String url = generateURL();
        String auth = null;
        try {
            auth = "BASIC " + AppConfig.getAuthCredentials(AppConfig.RXCONNECT_CREDENTIALS_FILE);
        } catch (IOException e) {
            // Nothing happens. We try without authentication
        }

        HttpRequest.Response resp = http.doPostGeneric(url, auth);
        if (resp.getResponseCode()>= 400) throw new I2ME2Exception("RXConnect error. Error code: " +
                resp.getResponseCode());
        return resp.getContent();
    }

    private String generateURL() {
        StringBuffer sb = new StringBuffer();
        sb.append(AppConfig.URL_RXCONNECT);
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
        Validator.NotNullEmptyStr(this.firstName, "RX " + PARAM_FIRST_NAME);
        Validator.NotNullEmptyStr(this.lastName, "RX " + PARAM_LAST_NAME);

        Validator.validDate(this.birthDate, PARAM_BIRTH_OF_DATE_FORMAT, "RX " + PARAM_BIRTH_DATE);

        List<String> opt = new ArrayList<>();
        opt.add("M");
        opt.add("F");
        Validator.validOptions(this.gender, opt, "RX " + PARAM_GENDER);
        Validator.validRegExp(this.zipCode, PARAM_ZIP_CODE_REGEXP, "RX " + PARAM_ZIP_CODE);
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

    //**************************
    // For testing purposes only
    //**************************
    public void setHttp(HttpRequest http) {
        this.http = http;
    }
}
