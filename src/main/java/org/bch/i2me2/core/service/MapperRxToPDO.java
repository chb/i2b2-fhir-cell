package org.bch.i2me2.core.service;

import org.bch.i2me2.core.config.AppConfig;
import org.bch.i2me2.core.exception.I2ME2Exception;
import org.bch.i2me2.core.util.mapper.Mapper;
import org.bch.i2me2.core.util.mapper.Mapper.XmlPdoObservationTag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.xml.txw2.annotation.XmlElement;

import javax.ejb.Stateless;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * IME-28
 * Mapping between a given RxJSON string to PDO XML
 * Created by CH176656 on 3/10/2015.
 */
@Stateless
public class MapperRxToPDO extends Mapper {

    // File names where claim and fill modifier_cd are listed
    private static final String CLAIM_MODIFIERS_FILE = "claimModifiers.i2me2";
    private static final String FILL_MODIFIERS_FILE = "fillModifiers.i2me2";

    // File name containing the list of real modifier codes
    private static final String REAL_MODIFIERS_FILE = "modifierCodes.i2me2";

    // The map between internal Modifier_CDs and real ones. Found in modifierCodes.i2me2 file
    // Each line: Internal_Modifier_CD,Real_Modifier_CD
    private HashMap<String, String> realModifiersCD;

    private List<String> fillsModifiers;
    private List<String> claimModifiers;

    // Some RX names
    //private static final String RX_HISTORYSEGMENT = "RxHistorySegments";
    private static final String RX_HISTORYSEGMENT_RX_RECS_RETURNED = "rxRecsReturned";
    private static final String RX_HISTORYSEGMENT_STATUS_MSG = "statusMsg";
    private static final String RX_ORDERS = "orders";

    private static final String PATIENTSEGMENTS = "PatientSegments";
    private static final String PATIENTSEGMENTS_DOB = "patientDOB";
    private static final String PATIENTSEGMENTS_GENDER = "patientGender";
    private static final String PATIENTSEGMENTS_SUBJECTID = "patientId";
    private static final String PATIENTSEGMENTS_SOURCESYSTEM = "patientIdSourceSystemName";
    private static final String PATIENTSEGMENTS_SOURCESYSTEM_EVENT = "eventIdSourceSystemName";

    private static final String RXD_DATETIME_KEY = "rxd.dateTime";
    private static final String RXD_DISPENSE_CODE_ID_KEY = "rxd.dispenseCodeIdentifier";
    private static final String PATIENTSEGMENTS_DOB_KEY = PATIENTSEGMENTS + "."+ PATIENTSEGMENTS_DOB;
    private static final String RX_HISTORYSEGMENT_MSGDATETIME_KEY = "msgDateTime";

    // Used to discriminate between claim and fills
    private static final String ORD_ENTERING_ORGANIZATION_ALTERNATIVE_ID_KEY = "orc.enteringOrganizationAlternativeId";

    // The above key value will store CLAIM_VALUE if it is a Claim
    private static final String CLAIM_VALUE = "Claim";
    
    // The concept_cd code when no NDC code is found
    private final String NO_CONCEPT_CD = "NO_NDC_CODE";

    private static String MODULE = "[RX_TO_PDO]";
    private static String OP_VALIDATE = "[VALIDATE]";
    private static String OP_LOAD_MODIFIERS = "[LOAD_MODIFIERS]";
    private static String OP_GET_PDOXML = "[GET_PDOXML]";
    private static String OP_FORMAT_DATETIME = "[FORMAT_DATETIME]";


    public MapperRxToPDO() {
        super();
        this.addKeyToFormat(RXD_DATETIME_KEY);
        this.addKeyToFormat(PATIENTSEGMENTS_DOB_KEY);
        this.addKeyToFormat(RX_HISTORYSEGMENT_MSGDATETIME_KEY);
    }

    /**
     * IME-28 Description
     * @param rxJson    The rx_json paramater
     * @param subjectId The subject id
     * @param dob       The DOB
     * @param gender    The gender
     * @return          The XML PDO
     * @throws I2ME2Exception See IME-28
     */
    public String getPDOXML(String rxJson, String subjectId, String dob, String gender, String source,
                            String sourceEvent) throws I2ME2Exception {
        this.log(Level.INFO, MODULE+OP_GET_PDOXML+"IN");
        String result;
        validate(subjectId, dob, gender, source, sourceEvent);
        try {
            loadModifiers();
            loadRealModifiers();
            String jsonExtra = generatePatientInfo(subjectId, dob, gender, source, sourceEvent);
            result = doMap(rxJson,PATIENTSEGMENTS, jsonExtra);
        } catch (I2ME2Exception e) {
            this.log(Level.SEVERE, MODULE+OP_GET_PDOXML+e.getMessage());
            throw e;
        } catch (Exception e) {
        	//e.printStackTrace();
            this.log(Level.SEVERE, MODULE+OP_GET_PDOXML+e.getMessage());
            throw new I2ME2Exception(e.getMessage(), e);
        }
        return result;
    }

    private String generatePatientInfo(String subjectId, String dob, String gender, String source, String sourceEvent) {
        String outJson = "{";
        outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_SUBJECTID, subjectId,false) + ",";
        if (dob!=null) {
            outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_DOB, dob, false) + ",";
        }
        if (gender!=null) {
            outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_GENDER, gender, true) + ",";
        }
        outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_SOURCESYSTEM, source,true) + ",";
        outJson = outJson + formatKeyValueJSON(PATIENTSEGMENTS_SOURCESYSTEM_EVENT, sourceEvent,true);
        return outJson + "}";
    }

    private String formatKeyValueJSON(String key, String value, boolean isText) {
        if (isText) {
            return "\"" + key + "\" : \"" + value + "\"";
        } else {
            return "\"" + key + "\" : " + value + "";
        }
    }

    private void validate(String subjectId, String dob, String gender, String source,
                          String sourceEvent) throws I2ME2Exception {
        try {
            if (subjectId==null) throw new I2ME2Exception("SubjectId cannot be null");
            if (subjectId.trim().equals("")) throw new I2ME2Exception("SubjectId cannot be empty");
            try {
                Long.parseLong(subjectId.trim());
            } catch (NumberFormatException e) {
                throw new I2ME2Exception("SubjectId must be numeric", e);
            }

            if (dob!=null) {
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
            }

            if (gender!=null) {
                String aux = gender.trim();
                if (aux.length() > 1) throw new I2ME2Exception("Bad Gender format. Must be 'M' 'F' or empty string");
                if (aux.length() == 1) {
                    if (!(aux.equals("F") || aux.equals("M") || aux.equals("I"))) {
                        throw new I2ME2Exception("Bad Gender format. Must be 'M', 'F', 'I,  or empty string");
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

    @Override
    protected String filterExtra(String xmlElem, Map<String, String> jsonDataMap, Map<String, String> jsonDataMapInArray, XmlPdoTag tag) {
        // We do not filter anything but Observations
        if (!tag.toString().equals(XmlPdoTag.TAG_OBSERVATIONS.toString())){
            return xmlElem;
        }

        // We also eliminate observations whose modifier_cd is rxRecsReturned and statusMsg
        String modifier_cd = getModifierCode(xmlElem);
        if (modifier_cd.trim().equals(RX_HISTORYSEGMENT_RX_RECS_RETURNED)) return "";
        if (modifier_cd.trim().equals(RX_HISTORYSEGMENT_STATUS_MSG)) return "";

        // filter claims/fills depending on orc.enteringOrganizationAlternativeId
        boolean isFill=true;
        if (jsonDataMapInArray.containsKey(ORD_ENTERING_ORGANIZATION_ALTERNATIVE_ID_KEY)) {
            String val = jsonDataMapInArray.get(ORD_ENTERING_ORGANIZATION_ALTERNATIVE_ID_KEY);
            isFill = val == null || !(val.trim().equals(CLAIM_VALUE));
        }

        // At this point we now whether it is a claim or a fill.

        boolean isInClaim = this.claimModifiers.contains(modifier_cd.trim());
        boolean isInFill = this.fillsModifiers.contains(modifier_cd.trim());

        if (isInClaim && isFill) return "";
        if (isInFill && !isFill) return "";

        // We place empty concept is NDC code is not found
        if (!jsonDataMapInArray.containsKey(RXD_DISPENSE_CODE_ID_KEY)) {
        	// If concept_cd is not set or it is empty!
            xmlElem = placeEmptyConceptCD(xmlElem);
        } else {
        	String val=jsonDataMapInArray.get(RXD_DISPENSE_CODE_ID_KEY);
        	if (val == null) {
        		xmlElem = placeEmptyConceptCD(xmlElem);
        	} else if (val.trim().equals("")) {
        		xmlElem = placeEmptyConceptCD(xmlElem);
        	}
        }
        return xmlElem;
    }

    private String placeEmptyConceptCD(String elem) {
    	String newElem = elem;
    	String conceptCD = this.getTagValueLine(newElem, XmlPdoObservationTag.TAG_CONCEPT_CD.toString());
    	String newConceptCD = "<" + XmlPdoObservationTag.TAG_CONCEPT_CD.toString() + ">";
        newConceptCD = newConceptCD + NO_CONCEPT_CD;
        newConceptCD = newConceptCD + "</" + XmlPdoObservationTag.TAG_CONCEPT_CD.toString() + ">";
        newElem = newElem.replaceAll(conceptCD, newConceptCD);
        return newElem;
    }
    
    private String getModifierCode(String xmlElem) {
        String modifier_cd_line = this.getTagValueLine(xmlElem, XmlPdoObservationTag.TAG_MODIFIER_CD.toString());
        String modifier_cd = modifier_cd_line.replace("<"+XmlPdoObservationTag.TAG_MODIFIER_CD.toString()+">","");
        modifier_cd = modifier_cd.replace("</"+XmlPdoObservationTag.TAG_MODIFIER_CD.toString()+">","");
        return modifier_cd;
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
        //JSONObject rxHistorySegments = root.getJSONObject(RX_HISTORYSEGMENT);
        return root.getJSONArray(RX_ORDERS);
    }

    /**
     * Reformat value according specifications. Returns the same value if value does not follow the structure.
     * @param value Precondition: value is MMM d, yyyy H:m:s a (i.e. 'Feb 12, 2015 12:00:00 AM')
     * @return      PostCondition: yyyy-MM-dd'T'HH:mm:ss.SS (i.e. '2015-02-12T12:00:00.00')
     */
    private String formatDateTime(String value) {
        String inputDataFormat = "MMM d, yyyy H:m:s a";
        try {
            String outputDataFormat = AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2);
            SimpleDateFormat dateFormatInput = new SimpleDateFormat(inputDataFormat);
            SimpleDateFormat dateFormatOutput = new SimpleDateFormat(outputDataFormat);
            Date date = dateFormatInput.parse(value);
            return dateFormatOutput.format(date);
        } catch (ParseException e) {
            this.log(Level.WARNING, MODULE+OP_FORMAT_DATETIME+"Parse datetime exception when formatting value:" +
                    value +". No formating");
            return value;
        } catch (Exception ee) {
            this.log(Level.WARNING, MODULE+OP_FORMAT_DATETIME+"Unknown exception datetime formatting value:" +
                    value + ". Returning empty string");
            return "";
        }
    }

    /**
     * Reformat value according to the specifications. Returns the same value if value does not follow the structure.
     * @param value Precondition: value is YYYYMMDD
     * @return      Postcondition: YYYY-MM-DD
     */
    private String formatDOB(String value) {
        if (value!=null) {
            if (value.length() != 8) {
                return value;
            }
            String newValue = value.substring(0, 4) + "-";
            newValue = newValue + value.substring(4, 6) + "-";
            newValue = newValue + value.substring(6, 8);
            return newValue;
        }
        return null;
    }

    @Override
    public String placeRealModifiersCodes(String text) {
        if (this.realModifiersCD==null) {
            return text;
        }
        String out = text;
        Set<String> keys = this.realModifiersCD.keySet();
        for(String key: keys) {
            String value = this.realModifiersCD.get(key);
            String currentFullLine = buildModifierLine(key);
            String realFullLine = buildModifierLine(value);
            out = out.replaceAll(currentFullLine, realFullLine);
        }
        return out;
    }

    private String buildModifierLine(String modifierCD) {
        String out = "<" + XmlPdoObservationTag.TAG_MODIFIER_CD.toString()+">";
        out = out + modifierCD + "</" + XmlPdoObservationTag.TAG_MODIFIER_CD.toString()+">";
        return out;
    }

    private void loadRealModifiers() throws Exception {
        try {
            String realModifiers = readTextFile(REAL_MODIFIERS_FILE, ",");
            String [] modifiers = realModifiers.split(",");
            this.realModifiersCD = new HashMap<>();
            for (String modifier: modifiers){
                String [] codes = modifier.split(":");
                this.realModifiersCD.put(codes[0].trim(), codes[1].trim());
            }
        } catch (Exception e) {
            this.log(Level.SEVERE, MODULE+OP_LOAD_MODIFIERS+"Error loading real modifiers. Error message:"
                    + e.getMessage());
            throw e;
        }
    }

    private void loadModifiers() throws Exception {
        try {
            String fillModifiers = readTextFile(FILL_MODIFIERS_FILE, ",");
            String claimModifiers = readTextFile(CLAIM_MODIFIERS_FILE, ",");

            String [] fills = fillModifiers.split(",");
            this.fillsModifiers = new ArrayList<>();
            for (String name : fills) {
                if (!name.trim().equals("")) {
                    this.fillsModifiers.add(name);
                }
            }

            String [] claims = claimModifiers.split(",");
            this.claimModifiers = new ArrayList<>();
            for (String name : claims) {
                if (!name.trim().equals("")) {
                    this.claimModifiers.add(name);
                }
            }
        } catch (Exception e) {
            this.log(Level.SEVERE, MODULE+OP_LOAD_MODIFIERS+"Error loading modifiers. Error message:" + e.getMessage());
            throw e;
        }
    }

    private String readTextFile(String fileName, String sep) throws Exception {
        InputStream in = MapperRxToPDO.class.getResourceAsStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sBuffer = new StringBuilder();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                sBuffer.append(line).append(sep);
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw e;

        } finally {
            in.close();
        }
        return sBuffer.toString();
    }
}

