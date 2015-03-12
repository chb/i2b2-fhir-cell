package org.bch.i2me2.core.service;

import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.util.mapper.Mapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * IME-28
 * Mapping between a given RxJSON string to PDO XML
 * Created by CH176656 on 3/10/2015.
 */
public class MapperRxToPDO extends Mapper{

    // Some RX names
    private static final String RX_HISTORYSEGMENT = "RxHistorySegments";
    private static final String RX_ORDERS = "orders";

    private static final String PATIENTSEGMENTS = "PatientSegments";
    private static final String PATIENTSEGMENTS_DOB = "patientDOB";
    private static final String PATIENTSEGMENTS_GENDER = "patientGender";
    private static final String PATIENTSEGMENTS_SUBJECTID = "patientId";
    private static final String PATIENTSEGMENTS_ZIP = "patientPostalCode";
    private static final String PATIENTSEGMENTS_SOURCESYSTEM = "patientIdSourceSystemName";

    private static final String RXD_DATETIME_KEY = "rxd.dateTime";
    private static final String PATIENTSEGMENTS_DOB_KEY = PATIENTSEGMENTS + "."+ PATIENTSEGMENTS_DOB;
    private static final String RX_HISTORYSEGMENT_MSGDATETIME_KEY = "RxHistorySegments.msgDateTime";

    MapperRxToPDO() {
        super();
        this.addKeyToFormat(RXD_DATETIME_KEY);
        this.addKeyToFormat(PATIENTSEGMENTS_DOB_KEY);
        this.addKeyToFormat(RX_HISTORYSEGMENT_MSGDATETIME_KEY);
    }

    /**
     * IME-28 Description
     * @param rxJson    The rx_json paramater
     * @param subjectId The subject id
     * @param zip       The ZIP code
     * @param dob       The DOB
     * @param gender    The gender
     * @return          The XML PDO
     * @throws I2ME2Exception See IME-28
     */
    public String getPDOXML(String rxJson, String subjectId, String zip, String dob, String gender, String source) throws I2ME2Exception {
        String result;
        validate(subjectId, zip, dob, gender);
        try {
            String jsonExtra = generatePatientInfo(subjectId, zip, dob, gender, source);
            result = doMap(rxJson,PATIENTSEGMENTS, jsonExtra);
        } catch (I2ME2Exception e) {
            //e.printStackTrace();
            throw e;
        } catch (Exception e) {
            //e.printStackTrace();
            throw new I2ME2Exception(e.getMessage(), e);
        }
        return result;
    }

    private String generatePatientInfo(String subjectId, String zip, String dob, String gender, String source) {
        String outJson = "{";
        outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_SUBJECTID, subjectId,false) + ",";
        outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_ZIP, zip, false)+ ",";
        outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_DOB, dob, false)+ ",";
        outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_GENDER, gender,true) + ",";
        outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_SOURCESYSTEM, source,true);
        return outJson + "}";
    }

    private String formatKeyValueJSON(String key, String value, boolean isText) {
        if (isText) {
            return "\"" + key + "\" : \"" + value + "\"";
        } else {
            return "\"" + key + "\" : " + value + "";
        }
    }

    private void validate(String subjectId, String zip, String dob, String gender) throws I2ME2Exception {
        if (subjectId==null) throw new I2ME2Exception("SubjectId cannot be null");
        if (subjectId.trim().equals("")) throw new I2ME2Exception("SubjectId cannot be empty");
        try {
            Long.parseLong(subjectId.trim());
        } catch (NumberFormatException e) {
            throw new I2ME2Exception("SubjectId must be numeric", e);
        }
        if (zip==null) throw new I2ME2Exception("zip cannot be null");
        if (zip.trim().length()<5) throw new I2ME2Exception("Bad zip code");
        try {
            Long.parseLong(zip.trim());
        } catch (NumberFormatException e) {
            throw new I2ME2Exception("Zip code must be numeric", e);
        }
        if (dob==null) throw new I2ME2Exception("DOB cannot be null");
        String inputDataFormat = "yyyyMMdd";
        String inputDataFormat2 = "yyyy-MM-dd";
        SimpleDateFormat dateFormatInput = new SimpleDateFormat(inputDataFormat);
        SimpleDateFormat dateFormatInput2 = new SimpleDateFormat(inputDataFormat2);

        Date date;
        try {
            date = dateFormatInput2.parse(dob);
            if (!dateFormatInput2.format(date).equals(dob)) {
                throw new I2ME2Exception("Invalid date:" + dob);
            }
        } catch (ParseException ee) {
            try {
                date = dateFormatInput.parse(dob);
                if (!dateFormatInput.format(date).equals(dob)) {
                    throw new I2ME2Exception("Invalid date:" + dob);
                }
            } catch (ParseException e) {
                throw new I2ME2Exception("Bad DOB format. Must be yyyyMMdd or yyyy-MM-dd", ee);
            }
        }

        if (gender==null) throw new I2ME2Exception("Gender cannot be null");
        String aux = gender.trim();
        if (aux.length()>1) throw new I2ME2Exception("Bad Gender format. Must be 'M' 'F' or empty string");
        if (aux.length()==1) {
            if (!(aux.equals("F") || aux.equals("M"))) {
                throw new I2ME2Exception("Bad Gender format. Must be 'M' 'F' or empty string");
            }
        }
    }

    @Override
    protected String filterExtra(String xmlElem, Map<String, String> jsonDataMap, Map<String, String> jsonDataMapInArray, XmlPdoTag tag) {
        // We do not filter anything but Observations
        if (!tag.toString().equals(XmlPdoTag.TAG_OBSERVATIONS.toString())){
            return xmlElem;
        }
        // we eliminate observations that have not been updated
        String tval = this.getTagValueLine(xmlElem, XmlPdoObservationTag.TAG_TVAL);
        String nval = this.getTagValueLine(xmlElem, XmlPdoObservationTag.TAG_NVAL);
        if (isNotSet(tval) || isNotSet(nval)) {
            // We do not want observations that has not been set.
            return "";
        }
        return xmlElem;
    }

    /**
     * return true if the value has not been set, so, if there is still the template information
     * @param value The field to check
     * @return True if the value has not been set. i.e, if F__ and __F are the delimiters, returns true is found both
     * of them.
     */
    private boolean isNotSet(String value) {
        if (value==null) return false;
        return (value.indexOf(this.getDelPre())>0 && value.indexOf(this.getDepPost())>0);
    }

    @Override
    protected String format(String key, String value, Map<String, String> dataMap, Map<String, String> dataMapInArray) {
        String newValue = value;
        switch(key) {
            case RXD_DATETIME_KEY:
                newValue = formatDateTime(value);
                break;
            case PATIENTSEGMENTS_DOB_KEY:
                newValue = formatDOB(value);
                break;
            case RX_HISTORYSEGMENT_MSGDATETIME_KEY:
                newValue = formatDateTime(value);
                break;
        }
        return newValue;
    }

    @Override
    protected JSONArray getJSONArray(JSONObject root) throws JSONException {
        JSONObject rxHistorySegments = root.getJSONObject(RX_HISTORYSEGMENT);
        return rxHistorySegments.getJSONArray(RX_ORDERS);
    }

    /**
     * Reformat value according specifications. Returns the same value if value does not follow the structure.
     * @param value Precondition: value is MMM d, yyyy H:m:s a (i.e. 'Feb 12, 2015 12:00:00 AM')
     * @return      PostCondition: yyyy-MM-dd'T'HH:mm:ss.SS (i.e. '2015-02-12T12:00:00.00')
     */
    private String formatDateTime(String value) {
        String inputDataFormat = "MMM d, yyyy H:m:s a";
        String outputDataFormat = "yyyy-MM-dd'T'HH:mm:ss.SS";

        SimpleDateFormat dateFormatInput = new SimpleDateFormat(inputDataFormat);
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat(outputDataFormat);

        try {
            Date date = dateFormatInput.parse(value);
            String newValue = dateFormatOutput.format(date);
            return newValue;
        } catch (ParseException e) {
            return value;
        } catch (Exception ee) {
            return "";
        }
    }

    /**
     * Reformat value according to the specifications. Returns the same value if value does not follow the structure.
     * @param value Precondition: value is YYYYMMDD
     * @return      Postcondition: YYYY-MM-DD
     */
    private String formatDOB(String value) {
        if (value.length()!=8) {
            return value;
        }
        String newValue = value.substring(0,4) + "-";
        newValue = newValue + value.substring(4,6) + "-";
        newValue = newValue + value.substring(6,8);
        return newValue;
    }
}

