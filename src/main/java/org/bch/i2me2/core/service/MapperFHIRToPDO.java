package org.bch.i2me2.core.service;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.util.mapper.Mapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created by CH176656 on 4/10/2015.
 */
public class MapperFHIRToPDO extends MapperI2ME2 {
    // The json tag containing the array of reconciled medications
    private static String MEDREC_MEDICATIONS = "medications";

    private static final String PATIENTSEGMENTS = "PatientSegments";
    private static final String PATIENTSEGMENTS_GENDER = "patientGender";
    private static final String PATIENTSEGMENTS_SUBJECTID = "patientId";
    private static final String PATIENTSEGMENTS_SOURCESYSTEM = "patientIdSourceSystemName";
    private static final String PATIENTSEGMENTS_SOURCESYSTEM_EVENT = "eventIdSourceSystemName";
    private static final String PATIENTSEGMENTS__EVENTID = "eventId";
    private static final String PATIENTSEGMENTS__MSG_DATETIME = "msgDateTime";

    private static final String RECS_RETURNED = "recsReturnedStatement";
    private static final String STATUS_MSG = "statusMsgStatement";
    private static final String MEDICATION_CODE_KEY = "medicationStatement.medication.code";

    private static final String XML_MAP_TEMPLATE_FILE = "xmlpdoTemplateMedRec.xml";

    private static String MODULE = "[FHIR_TO_PDO]";
    private static String OP_GET_PDOXML = "[GET_PDOXML]";
    private static String OP_VALIDATE = "[VALIDATE]";
    private static String OP_GENERATE_PATIENT_INFO = "[GENERATE_PATIENT_INFO]";
    /**
     * IME-30 Description
     * @param rxJson    The rx_json paramater
     * @param subjectId The subject id
     * @param gender    The gender
     * @return          The XML PDO
     * @throws org.bch.i2me2.core.exception.I2ME2Exception See IME-30
     */
    public String getPDOXML(String rxJson, String subjectId, String gender, String source,
                            String sourceEvent, Date dateMsg) throws I2ME2Exception {
        this.log(Level.INFO, MODULE+OP_GET_PDOXML+"IN");
        String result;
        validate(subjectId, gender, source, sourceEvent);
        try {
            loadRealModifiers();
            String jsonExtra = generatePatientInfo(subjectId, gender, source, sourceEvent, dateMsg);
            result = doMap(rxJson,PATIENTSEGMENTS, jsonExtra);
        } catch (I2ME2Exception e) {
            this.log(Level.SEVERE, MODULE+OP_GET_PDOXML+e.getMessage());
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            this.log(Level.SEVERE, MODULE+OP_GET_PDOXML+e.getMessage());
            throw new I2ME2Exception(e.getMessage(), e);
        }
        return result;
    }

    private void validate(String subjectId, String gender, String source,
                          String sourceEvent) throws I2ME2Exception {
        try {
            if (subjectId==null) throw new I2ME2Exception("SubjectId cannot be null");
            if (subjectId.trim().equals("")) throw new I2ME2Exception("SubjectId cannot be empty");
            try {
                Long.parseLong(subjectId.trim());
            } catch (NumberFormatException e) {
                throw new I2ME2Exception("SubjectId must be numeric", e);
            }
            if (gender!=null) {
                String aux = gender.trim();
                if (aux.length() > 1) throw new I2ME2Exception("Bad Gender format. Must be 'M' 'F' or empty string");
                if (aux.length() == 1) {
                    if (!(aux.equals("F") || aux.equals("M"))) {
                        throw new I2ME2Exception("Bad Gender format. Must be 'M' 'F' or empty string");
                    }
                }
            }
            if (source==null) throw new I2ME2Exception("Source cannot be null");
            if (source.trim().equals("")) throw new I2ME2Exception("Source cannot be empty");
            if (sourceEvent==null) throw new I2ME2Exception("Source event cannot be null");
            if (sourceEvent.trim().equals("")) throw new I2ME2Exception("Source event cannot be empty");
        } catch (I2ME2Exception e) {
            this.log(Level.SEVERE, MODULE+OP_VALIDATE+e.getMessage());
            throw e;
        }
    }

    private String generatePatientInfo(String subjectId, String gender, String source, String sourceEvent, Date dateMsg) {
        String outJson = "{";
        outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_SUBJECTID, subjectId,false) + ",";
        if (gender!=null) {
            outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_GENDER, gender, true) + ",";
        }
        outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_SOURCESYSTEM, source,true) + ",";
        outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_SOURCESYSTEM_EVENT, sourceEvent,true)+ ",";

        // We now generate eventId and msgDateTime
        try {
            String outputDataFormat = AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2);
            SimpleDateFormat dateFormatOutput = new SimpleDateFormat(outputDataFormat);
            String dateTime = dateFormatOutput.format(dateMsg);
            outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS__MSG_DATETIME, dateTime,true) + ",";
        } catch (Exception e) {
            this.log(Level.SEVERE,MODULE+OP_GENERATE_PATIENT_INFO+e.getMessage());
            this.log(Level.WARNING,MODULE+OP_GENERATE_PATIENT_INFO+"Error formatting dateTime");
        }

        Long eventId = (dateMsg).getTime();
        outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS__EVENTID, eventId.toString(),false);

        return outJson + "}";
    }

    @Override
    protected String filterExtra(String xmlElem, Map<String, String> jsonDataMap, Map<String, String> jsonDataMapInArray, XmlPdoTag tag) {
        // We do not filter anything but Observations
        if (!tag.toString().equals(XmlPdoTag.TAG_OBSERVATIONS.toString())){
            return xmlElem;
        }

        // We also eliminate observations whose modifier_cd is rxRecsReturned and statusMsg
        String modifier_cd = getModifierCode(xmlElem);
        if (modifier_cd.trim().equals(RECS_RETURNED)) return "";
        if (modifier_cd.trim().equals(STATUS_MSG)) return "";


        // We place empty concept in NDC code if not found
        if (!jsonDataMapInArray.containsKey(MEDICATION_CODE_KEY)) {
            // If concept_cd is not set or it is empty!
            xmlElem = placeEmptyConceptCD(xmlElem);
        } else {
            String val=jsonDataMapInArray.get(MEDICATION_CODE_KEY);
            if (val == null) {
                xmlElem = placeEmptyConceptCD(xmlElem);
            } else if (val.trim().equals("")) {
                xmlElem = placeEmptyConceptCD(xmlElem);
            }
        }
        return xmlElem;
    }


    @Override
    protected JSONArray getJSONArray(JSONObject root) throws JSONException {
        return root.getJSONArray(MEDREC_MEDICATIONS);
    }

    MapperFHIRToPDO() {
        super();
        this.setXmlMapFileTemplate(XML_MAP_TEMPLATE_FILE);
    }

}
