package org.bch.fhir.i2b2.service;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import org.bch.fhir.i2b2.config.AppConfig;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.bch.fhir.i2b2.pdomodel.Element;
import org.bch.fhir.i2b2.pdomodel.ElementSet;
import org.bch.fhir.i2b2.pdomodel.PDOModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: CH176656
 * Date: 7/22/15
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class FHIRToPDO {
    public abstract String getPDOXML(BaseResource resource) throws FHIRI2B2Exception;

    public static final String FHIR_TAG_VALUE_QUANTITY = "valueQuantity";
    public static final String FHIR_TAG_VALUE_STRING = "valueString";
    public static final String FHIR_TAG_VALUE_INTEGER = "valueInteger";
    public static final String FHIR_TAG_VALUE_CODING = "valueCoding";
    public static final String FHIR_TAG_VALUE_BOOLEAN = "valueBoolean";

    public static final String DEFAULT_PATIENT_SOURCE = "BCH";
    public static final String DEFAULT_EVENT_SOURCE = "BCH";

    protected String patientIdeSource=DEFAULT_PATIENT_SOURCE;
    protected String patientIde=null;
    protected String eventIdeSource=DEFAULT_EVENT_SOURCE;
    protected String eventIde=null;

    protected String getEventId(Encounter enc) throws FHIRI2B2Exception {
        String eventId = null;
        if (enc.getId().isEmpty()) {
            eventId = "" + new Date().getTime();
        } else {
            // TODO: Provisional!!
            eventId = "" + new Date().getTime();
            //eventId = enc.getId().getIdPart();
        }
        return eventId;
    }

    protected ElementSet generateEventSet(Encounter enc) throws FHIRI2B2Exception {
        ElementSet eventSet = new ElementSet();
        eventSet.setTypePDOSet(ElementSet.PDO_EVENT_SET);
        Element event = new Element();
        event.setTypePDO(Element.PDO_EVENT);

        String pdoEventId = generateRow(PDOModel.PDO_EVENT_ID, this.eventIde,genParamStr(PDOModel.PDO_SOURCE, this.eventIdeSource));
        String pdoPatientId = generateRow(PDOModel.PDO_PATIENT_ID, this.patientIde,
                genParamStr("source", this.patientIdeSource));
        event.addRow(pdoEventId);
        event.addRow(pdoPatientId);

        if (!enc.getPeriod().isEmpty()) {
            PeriodDt period = enc.getPeriod();
            Date startDate = period.getStart();
            Date endDate = period.getEnd();
            if (startDate!=null) {
                String outputDataFormat = AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2);
                SimpleDateFormat dateFormatOutput = new SimpleDateFormat(outputDataFormat);
                String startDateStr = dateFormatOutput.format(startDate);

                String pdoStartDate = generateRow(PDOModel.PDO_START_DATE, startDateStr);
                event.addRow(pdoStartDate);
            }
            if (endDate!=null) {
                String outputDataFormat = AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2);
                SimpleDateFormat dateFormatOutput = new SimpleDateFormat(outputDataFormat);
                String endDateStr = dateFormatOutput.format(endDate);

                String pdoEndDate = generateRow(PDOModel.PDO_END_DATE, endDateStr);
                event.addRow(pdoEndDate);
            }
        }
        eventSet.addElement(event);
        return eventSet;
    }

    protected ElementSet generateEIDSet() throws FHIRI2B2Exception {
        ElementSet eidSet = new ElementSet();
        eidSet.setTypePDOSet(ElementSet.PDO_EID_SET);
        Element eid = new Element();
        eid.setTypePDO(Element.PDO_EID);

        //<event_id patient_id="1234" patient_id_source="BCH" source="SCR">1423742400000</event_id>
        String pdoEventId = this.generateRow(PDOModel.PDO_EVENT_ID, this.eventIde,
                genParamStr(PDOModel.PDO_PATIENT_ID, this.patientIde),
                genParamStr(PDOModel.PDO_PATIENT_ID_SOURCE, this.patientIdeSource),
                genParamStr(PDOModel.PDO_SOURCE, this.eventIdeSource));

        eid.addRow(pdoEventId);
        eidSet.addElement(eid);
        return eidSet;
    }

    protected ElementSet generatePIDSet() throws FHIRI2B2Exception {
        ElementSet pidSet = new ElementSet();
        pidSet.setTypePDOSet(ElementSet.PDO_PID_SET);
        Element pid = new Element();
        pid.setTypePDO(Element.PDO_PID);

        //<patient_id source="BCH">1234</patient_id>
        String pdoPatientId = this.generateRow(PDOModel.PDO_PATIENT_ID, this.patientIde,
                genParamStr(PDOModel.PDO_SOURCE, this.patientIdeSource));

        pid.addRow(pdoPatientId);
        pidSet.addElement(pid);
        return pidSet;
    }

    protected ElementSet generatePatientSet() throws FHIRI2B2Exception {
        ElementSet patientSet = new ElementSet();
        patientSet.setTypePDOSet(ElementSet.PDO_PATIENT_SET);
        Element patient = new Element();
        patient.setTypePDO(Element.PDO_PATIENT);

        String pdoPatientId = this.generateRow(PDOModel.PDO_PATIENT_ID, this.patientIde,
                genParamStr(PDOModel.PDO_SOURCE, this.patientIdeSource));
        patient.addRow(pdoPatientId);
        patientSet.addElement(patient);
        return patientSet;
    }

    protected String finishRow(StringBuffer in, String tag, String value) {
        in.append(value);
        in.append("</").append(tag).append(">");
        return in.toString();
    }

    protected String generateRow(String tag, String value) {
        StringBuffer out = new StringBuffer();
        out.append("<").append(tag).append(">");
        return finishRow(out,tag, value);
    }

    protected String generateRow(String tag, String value, String param) {
        StringBuffer out = new StringBuffer();
        out.append("<").append(tag).append(" ").append(param).append(">");
        return finishRow(out,tag, value);
    }

    protected String generateRow(String tag, String value, String param1, String param2) {
        StringBuffer out = new StringBuffer();
        out.append("<").append(tag).append(" ").append(param1).append(" ").append(param2).append(">");
        return finishRow(out,tag, value);
    }

    protected String generateRow(String tag, String value, String param1, String param2, String param3) {
        StringBuffer out = new StringBuffer();
        out.append("<").append(tag).append(" ").append(param1).append(" ").
                append(param2).append(" ").append(param3).append(">");
        return finishRow(out,tag, value);
    }

    protected String genParamStr(String paramName, String valueStr) {
        return paramName + "=\"" + valueStr + "\"";
    }

    protected String genParamNum(String paramName, String valueNum) {
        return paramName + "=" + valueNum;
    }

    protected boolean isRawConceptCD(String type) {
        if (!type.equals(FHIR_TAG_VALUE_CODING)) return true;
        return false;
    }

    protected boolean isNumericType(String type) {
        if (type == null) return false;
        if (type.equals(FHIR_TAG_VALUE_QUANTITY)) return true;
        else if (type.equals(FHIR_TAG_VALUE_INTEGER)) return true;
        return false;
    }

    protected IResource findResourceById(List<IResource> resources, String id){
        int i=0;
        IResource out = null;
        while (i<resources.size() && out==null) {
            IResource res = resources.get(i);
            if (res.getId().getIdPart().equals(id)) {
                out = res;
            }
            i++;
        }
        return out;
    }

}
