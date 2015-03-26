package org.bch.i2me2.core.external;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.util.HttpRequest;
import org.bch.i2me2.core.util.Response;
import org.bch.i2me2.core.util.Utils;
import org.bch.i2me2.core.util.Validator;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by CH176656 on 3/23/2015.
 */
public class IDM extends WrapperAPI {

    public static String PARAM_TOKEN = "subject_token";

    public static String HTTP_AUTH_METHOD = "BASIC";
    public static String HTTP_TYPE_CONSUMES = "application/x-www-form-urlencoded";

    public static String FIRST_NAME_KEY = "firstName";
    public static String LAST_NAME_KEY = "lastName";
    public static String BIRTH_DATE_KEY = "birthDate";
    public static String GENDER_KEY = "sex";
    public static String ZIP_CODE_KEY = "zipCode";
    public static String SUBJECT_ID_KEY = "subjectId";

    private String token;

    /**
     *  Makes an http request to IDM to obtain personal data to query RXConnect
     * @param token             The JWS token
     * @return                  PersonalInfo with the returned info
     * @throws I2ME2Exception   If validation fails or if IDM returns 400 or higher
     * @throws IOException      If connection error
     */
    public PersonalInfo getPersonalInfo(String token) throws I2ME2Exception, IOException {
        return getInfo(token, AppConfig.getProp(AppConfig.EP_IDM_RESOURCE));
    }

    /**
     * Makes an http request to IDM to obtain subject id
     * @param token             The JWS token
     * @return                  A PersonalInfo structure with only subjectId informed
     * @throws I2ME2Exception   If validation fails or if IDM returns 400 or higher
     * @throws IOException      If connection error
     */
    public PersonalInfo getPersonalSubjectId(String token) throws I2ME2Exception, IOException {
        return getInfo(token, AppConfig.getProp(AppConfig.EP_IDM_ID));
    }

    private PersonalInfo getInfo(String token, String operation) throws I2ME2Exception, IOException {
        this.token = token;
        validate();
        String url = generateURL(operation);
        String content = generateContent();
        String auth = null;
        try {
            auth = HTTP_AUTH_METHOD + " " + AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_FILE_IDM);
        } catch (IOException e) {
            // Nothing happens. We try without authentication
        }
        Response resp = http.doPostGeneric(url, content, auth, HTTP_TYPE_CONSUMES);
        if (resp.getResponseCode()>= 400) throw new I2ME2Exception("IRM error. Error code: " +
                resp.getResponseCode());

        return parsePersonalInfo(resp.getContent());
    }

    private PersonalInfo parsePersonalInfo(String jsonInput) throws I2ME2Exception{
        PersonalInfo pi = new PersonalInfo();
        try {
            JSONObject jsonRoot = new JSONObject(jsonInput);
            pi.setFirstName(getJSONValue(jsonRoot,FIRST_NAME_KEY));
            pi.setLastName(getJSONValue(jsonRoot, LAST_NAME_KEY));
            pi.setBirthDate(getJSONValue(jsonRoot,BIRTH_DATE_KEY));
            pi.setGender(getJSONValue(jsonRoot, GENDER_KEY));
            pi.setZipCode(getJSONValue(jsonRoot, ZIP_CODE_KEY));
            pi.setSubjectId(getJSONValue(jsonRoot, SUBJECT_ID_KEY));

        } catch (JSONException e){
            throw new I2ME2Exception("json bad format", e);
        }
        return pi;
    }

    private String getJSONValue(JSONObject jsonObject, String key) {
        String value = null;
        try {
            value = jsonObject.getString(key);
        } catch (JSONException e) {
            try {
                long val = jsonObject.getLong(key);
                value = "" + val;
            } catch (JSONException ee) {
                // DO NOTHING
            }
        }
        return value;
    }

    private void validate() throws I2ME2Exception {
        Validator.NotNullEmptyStr(this.token, PARAM_TOKEN);
    }

    private String generateURL(String operation) throws I2ME2Exception {
        return Utils.generateURL(
                AppConfig.getProp(AppConfig.NET_PROTOCOL_IDM),
                AppConfig.getProp(AppConfig.HOST_IDM),
                AppConfig.getProp(AppConfig.PORT_IDM),
                operation);
    }

    private String generateContent() {
        return HttpRequest.urlParam(PARAM_TOKEN, this.token);
    }


    public static class PersonalInfo {
        private String subjectId = null;
        private String firstName=null;
        private String lastName = null;
        private String birthDate = null;
        private String gender = null;
        private String zipCode = null;

        public String getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(String subjectId) {
            this.subjectId = subjectId;
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
}
