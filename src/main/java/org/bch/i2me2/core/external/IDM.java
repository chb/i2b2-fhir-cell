package org.bch.i2me2.core.external;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.util.HttpRequest;
import org.bch.i2me2.core.util.Response;
import org.bch.i2me2.core.util.Utils;
import org.bch.i2me2.core.util.Validator;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by CH176656 on 3/23/2015.
 */
@Stateless
public class IDM extends WrapperAPI {

    public static String PARAM_TOKEN = "subject_token";

    public static String HTTP_AUTH_METHOD = "Basic";
    public static String HTTP_TYPE_CONSUMES = "application/x-www-form-urlencoded";

    public static String FIRST_NAME_KEY = "firstName";
    public static String LAST_NAME_KEY = "lastName";
    public static String BIRTH_DATE_KEY = "birthDate";
    public static String GENDER_KEY = "sex";
    public static String ZIP_CODE_KEY = "zipCode";
    public static String SUBJECT_ID_KEY = "subjectId";

    private static String MODULE = "[IDM]";
    private static String OP_GET_INFO = "[GET_INFO]";
    private static String OP_VALIDATE = "[VALIDATE]";
    private static String OP_PARSE_PER_INFO = "[PARSE_PERSONAL_INFO]";


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
        this.log(Level.INFO, MODULE+OP_GET_INFO+operation + ": IN");
        this.token = token;
        validate();
        String url = generateURL(operation);
        String content = generateContent();
        String auth = null;
        try {
            String cred = AppConfig.getAuthCredentials(AppConfig.CREDENTIALS_FILE_IDM);
            auth = HTTP_AUTH_METHOD + " " +
                    new String(javax.xml.bind.DatatypeConverter.printBase64Binary(cred.getBytes()));
        } catch (IOException e) {
            this.log(Level.WARNING, MODULE+OP_GET_INFO+ operation + ": " +
                    "No authentication credentials found for IDM. Trying without authentication");
        }
        Response resp = getHttpRequest().doPostGeneric(url, content, auth, HTTP_TYPE_CONSUMES);
        System.out.println(resp.getResponseCode());
        if (resp.getResponseCode()>= 400) {
            this.log(Level.SEVERE, MODULE+OP_GET_INFO + operation + ":" +
                    "IDM error. Error code: " + resp.getResponseCode());
            throw new I2ME2Exception("IDM error. Error code: " + resp.getResponseCode());
        }

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
            this.log(Level.SEVERE, MODULE+OP_PARSE_PER_INFO+ "Error parsing returned json:" + jsonInput);
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
        try {
            Validator.NotNullEmptyStr(this.token, PARAM_TOKEN);
        } catch (I2ME2Exception e) {
            this.log(Level.SEVERE, MODULE+OP_VALIDATE+e.getMessage());
            throw e;
        }
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
        private static String MALE = "male";
        private static String FEMALE = "female";
        private static String INTERSEXED = "intersexed";
        private static String UNKNOWN = "unknown";

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
            if (this.birthDate==null) return null;
            if (this.birthDate.length()>=10) return birthDate.substring(0,10);
            return birthDate;
        }

        public void setBirthDate(String birthDate) {
            this.birthDate = birthDate;
        }

        public String getGender() {
            if (this.gender==null) return null;
            if (this.gender.toLowerCase().equals(MALE.toLowerCase())) return "M";
            if (this.gender.toLowerCase().equals(FEMALE.toLowerCase())) return "F";
            if (this.gender.toLowerCase().equals(INTERSEXED.toLowerCase())) return "I";
            if (this.gender.toLowerCase().equals(UNKNOWN.toLowerCase())) return "";
            return this.gender;
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
