package org.bch.fhir.i2b2.service;

import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ContainedDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.QuestionnaireAnswers;
import org.bch.fhir.i2b2.config.AppConfig;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.bch.fhir.i2b2.pdomodel.Element;
import org.bch.fhir.i2b2.pdomodel.ElementSet;
import org.bch.fhir.i2b2.pdomodel.PDOModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Converts a QuestionnaireAnswer FHIR resource to the corresponding XMLPDO
 * Created by ipinyol on 7/9/15.
 */
public class QAnswersToI2B2 {


    public static final String DEFAULT_PATIENT_SOURCE = "BCH";
    public static final String DEFAULT_EVENT_SOURCE = "BCH";

    private String patientIdeSource=DEFAULT_PATIENT_SOURCE;
    private String patientIde=null;
    private String eventIdeSource=DEFAULT_EVENT_SOURCE;
    private String eventIde=null;

    public String getPDOXML(QuestionnaireAnswers qa) throws FHIRI2B2Exception {
        PDOModel pdo = new PDOModel();
        if (qa!=null) {
            this.patientIde = this.getPatientId(qa);
            Encounter enc = findEncounter(qa);
            this.eventIde = this.getEventId(enc);
            ElementSet eidSet = this.generateEIDSet();
            ElementSet pidSet = this.generatePIDSet();
            ElementSet eventSet = this.generateEventSet(enc);
            ElementSet patientSet = this.generatePatientSet();
            ElementSet observationSet = this.generateObservationSet(qa);
            pdo.addElementSet(eidSet);
            pdo.addElementSet(pidSet);
            pdo.addElementSet(eventSet);
            pdo.addElementSet(patientSet);
            pdo.addElementSet(observationSet);
        }
        return pdo.generatePDOXML();
    }

    private Encounter findEncounter(QuestionnaireAnswers qa) throws FHIRI2B2Exception{
        ResourceReferenceDt refEncounter = qa.getEncounter();
        if (refEncounter.isEmpty()) throw new FHIRI2B2Exception("Encounter reference is not informed");

        String idEnc = refEncounter.getReference().getIdPart();

        ContainedDt containedDt = qa.getContained();
        List<IResource> iResources = containedDt.getContainedResources();
        IResource encRes = findResourceById(iResources, idEnc);
        if (encRes == null) throw new FHIRI2B2Exception("Encounter reference not found in contained list");
        Encounter enc = (Encounter) encRes;
        return enc;
    }

    private String getPatientId(QuestionnaireAnswers qa) throws FHIRI2B2Exception {
        ResourceReferenceDt refPatient = qa.getSubject();
        if (refPatient.isEmpty()) throw new FHIRI2B2Exception("Subject reference is not informed");
        String idPat = refPatient.getReference().getIdPart();
        return idPat;
    }

    private String getEventId(Encounter enc) throws FHIRI2B2Exception {
        String eventId = null;
        if (enc.getId().isEmpty()) {
            eventId = "" + new Date().getTime();
        } else {
            eventId = enc.getId().getIdPart();
        }
        return eventId;
    }

    private ElementSet generateEventSet(Encounter enc) throws FHIRI2B2Exception {
        ElementSet eventSet = new ElementSet();
        eventSet.setTypePDOSet(ElementSet.PDO_EVENT_SET);
        Element event = new Element();
        event.setTypePDO(Element.PDO_EVENT);

        String pdoEventId = generateRow(PDOModel.PDO_EVENT_ID, this.eventIde,genParamStr("source", this.eventIdeSource));
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

    private ElementSet generateEIDSet() throws FHIRI2B2Exception {
        ElementSet eidSet = new ElementSet();
        eidSet.setTypePDOSet(ElementSet.PDO_EID_SET);
        Element eid = new Element();
        eid.setTypePDO(Element.PDO_EID);

        //<event_id patient_id="1234" patient_id_source="BCH" source="SCR">1423742400000</event_id>
        String pdoEventId = this.generateRow(PDOModel.PDO_EVENT_ID, this.eventIde,
                genParamStr(PDOModel.PDO_PATIENT_ID, this.patientIde),
                genParamStr("patient_id_source", this.patientIdeSource),
                genParamStr("source", this.eventIdeSource));

        eid.addRow(pdoEventId);
        eidSet.addElement(eid);
        return eidSet;
    }

    private ElementSet generatePIDSet() throws FHIRI2B2Exception {
        ElementSet pidSet = new ElementSet();
        pidSet.setTypePDOSet(ElementSet.PDO_PID_SET);
        Element pid = new Element();
        pid.setTypePDO(Element.PDO_PID);

        //<patient_id source="BCH">1234</patient_id>
        String pdoPatientId = this.generateRow(PDOModel.PDO_PATIENT_ID, this.patientIde,
                genParamStr("source", this.patientIdeSource));

        pid.addRow(pdoPatientId);
        pidSet.addElement(pid);
        return pidSet;
    }

    private ElementSet generatePatientSet() throws FHIRI2B2Exception {
        ElementSet patientSet = new ElementSet();
        patientSet.setTypePDOSet(ElementSet.PDO_PATIENT_SET);
        Element patient = new Element();
        patient.setTypePDO(Element.PDO_PATIENT);

        String pdoPatientId = this.generateRow(PDOModel.PDO_PATIENT_ID, this.patientIde,
                genParamStr("source", this.patientIdeSource));
        patient.addRow(pdoPatientId);
        patientSet.addElement(patient);
        return patientSet;
    }

    private ElementSet generateObservationSet(QuestionnaireAnswers qa) throws FHIRI2B2Exception {
        ElementSet observationSet = new ElementSet();
        observationSet.setTypePDOSet(ElementSet.PDO_OBSERVATION_SET);

        return observationSet;
    }

    private IResource findResourceById(List<IResource> resources, String id){
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

    private String finishRow(StringBuffer in, String tag, String value) {
        in.append(value);
        in.append("</").append(tag).append(">");
        return in.toString();
    }

    private String generateRow(String tag, String value) {
        StringBuffer out = new StringBuffer();
        out.append("<").append(tag).append(">");
        return finishRow(out,tag, value);
    }

    private String generateRow(String tag, String value, String param) {
        StringBuffer out = new StringBuffer();
        out.append("<").append(tag).append(" ").append(param).append(">");
        return finishRow(out,tag, value);
    }

    private String generateRow(String tag, String value, String param1, String param2) {
        StringBuffer out = new StringBuffer();
        out.append("<").append(tag).append(" ").append(param1).append(" ").append(param2).append(">");
        return finishRow(out,tag, value);
    }

    private String generateRow(String tag, String value, String param1, String param2, String param3) {
        StringBuffer out = new StringBuffer();
        out.append("<").append(tag).append(" ").append(param1).append(" ").
                append(param2).append(" ").append(param3).append(">");
        return finishRow(out,tag, value);
    }

    private String genParamStr(String paramName, String valueStr) {
        return paramName + "=\"" + valueStr + "\"";
    }

    private String genParamNum(String paramName, String valueNum) {
        return paramName + "=" + valueNum;
    }
}
