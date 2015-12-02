package org.bch.fhir.i2b2.service;

import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContainedDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.QuestionnaireResponse;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DecimalDt;
import ca.uhn.fhir.model.primitive.IntegerDt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bch.fhir.i2b2.config.AppConfig;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.bch.fhir.i2b2.pdomodel.Element;
import org.bch.fhir.i2b2.pdomodel.ElementSet;
import org.bch.fhir.i2b2.pdomodel.PDOModel;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Converts a QuestionnaireAnswer FHIR resource to the corresponding XMLPDO
 * Created by ipinyol on 7/9/15.
 */
public class QResponseToI2B2 extends FHIRToPDO {

    Log log = LogFactory.getLog(QResponseToI2B2.class);


    @Override
    public String getPDOXML(BaseResource resource) throws FHIRI2B2Exception {
        QuestionnaireResponse qa = (QuestionnaireResponse) resource;
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

            String ref = getQReference(qa);
            String metaInfo = "QuestionnaireAnswers#" + ref;
            addMetadataInObservationSet(metaInfo, METADATA_CONCEPT_CD, observationSet);
        }
        return pdo.generatePDOXML();
    }

    private String getQReference(QuestionnaireResponse qa) {
        ResourceReferenceDt questionnaireRef = qa.getQuestionnaire();
        String ret = null;
        if (questionnaireRef!=null) {
            if (questionnaireRef.getReference() != null) {
                ret = questionnaireRef.getReference().getIdPart();
            }
        }
        if (ret!=null) {
            if (ret.isEmpty()) ret = null;
        }

        if (ret == null) {
            QuestionnaireResponse.Group gr = qa.getGroup();
            if (gr != null) {
                ret = gr.getLinkId();
            }
        }

        return ret;
    }

    private Encounter findEncounter(QuestionnaireResponse qa) throws FHIRI2B2Exception{
        ResourceReferenceDt refEncounter = qa.getEncounter();
        if (refEncounter.isEmpty()) {
            log.warn("Encounter reference is not informed. We continue");
            return null;
        }

        String idEnc = refEncounter.getReference().getIdPart();

        ContainedDt containedDt = qa.getContained();
        List<IResource> iResources = containedDt.getContainedResources();
        IResource encRes = findResourceById(iResources, idEnc);
        if (encRes == null) {
            throw new FHIRI2B2Exception("Encounter reference not found in contained list");
        }
        Encounter enc = (Encounter) encRes;
        return enc;
    }

    private String getPatientId(QuestionnaireResponse qa) throws FHIRI2B2Exception {
        ResourceReferenceDt refPatient = qa.getSubject();
        if (refPatient.isEmpty()) throw new FHIRI2B2Exception("Subject reference is not informed");
        String idPat = refPatient.getReference().getIdPart();
        return idPat;
    }



    private ElementSet generateObservationSet(QuestionnaireResponse qa) throws FHIRI2B2Exception {
        ElementSet observationSet = new ElementSet();
        observationSet.setTypePDOSet(ElementSet.PDO_OBSERVATION_SET);

        QuestionnaireResponse.Group group = qa.getGroup();

        processGroup(group, observationSet);
        return observationSet;
    }

    private void processGroup(QuestionnaireResponse.Group group, ElementSet observationSet) throws FHIRI2B2Exception{
        if (group.getGroup().isEmpty() && group.getQuestion().isEmpty())
            throw new FHIRI2B2Exception("Group does not have any question nor group");

        if (!group.getGroup().isEmpty()) {
            List<QuestionnaireResponse.Group> groups = group.getGroup();
            for (QuestionnaireResponse.Group gr : groups) {
                processGroup(gr, observationSet);
            }
        } else {
            List<QuestionnaireResponse.GroupQuestion> questions = group.getQuestion();
            for (QuestionnaireResponse.GroupQuestion question: questions) {
                processQuestion(question, observationSet);
            }
        }
    }

    private void processQuestion(QuestionnaireResponse.GroupQuestion question, ElementSet observationSet)
            throws FHIRI2B2Exception {
        if (!question.getAnswer().isEmpty()) {
            List<QuestionnaireResponse.GroupQuestionAnswer> answers = question.getAnswer();
            String link = question.getLinkId();
            int i=1;
            for (QuestionnaireResponse.GroupQuestionAnswer answer: answers) {
                Element observation = generateObservation(answer, i, link);
                observationSet.addElement(observation);
                // TODO: test it
                if (!answer.getGroup().isEmpty()) {
                    for(QuestionnaireResponse.Group gr: answer.getGroup()) {
                        processGroup(gr, observationSet);
                    }
                }
            }
        }

        // This is the old version fhir
        /*
        if (!question.getGroup().isEmpty()) {
            for(QuestionnaireResponse.Group gr: question.getGroup()) {
                processGroup(gr, observationSet);
            }
        }
        */
    }

    private Element generateObservation(QuestionnaireResponse.GroupQuestionAnswer answer, int i, String link)
            throws FHIRI2B2Exception{
        Element out = new Element();
        out.setTypePDO(Element.PDO_OBSERVATION);
        Map<String, String> mapConceptCode = AppConfig.getRealConceptCodesMap();
        Map<String, String> mapConceptCodeType = AppConfig.getConceptCodesTypeMap();

        String pdoEventId = this.generateRow(PDOModel.PDO_EVENT_ID, this.eventIde,
                this.genParamStr(PDOModel.PDO_SOURCE, this.eventIdeSource));
        out.addRow(pdoEventId);

        String pdoPatientId = this.generateRow(PDOModel.PDO_PATIENT_ID, this.patientIde,
                this.genParamStr(PDOModel.PDO_SOURCE, this.patientIdeSource));
        out.addRow(pdoPatientId);

        String outputDataFormat = AppConfig.getProp(AppConfig.FORMAT_DATE_I2B2);
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat(outputDataFormat);
        String pdoStartDate = this.generateRow(PDOModel.PDO_START_DATE, dateFormatOutput.format(new Date()));
        out.addRow(pdoStartDate);

        String pdoObserverCd = generateRow(PDOModel.PDO_OBSERVER_CD, "@");
        out.addRow(pdoObserverCd);

        String pdoConceptCd = null;
        String conceptCd=null;

        String pdoInstanceNum = generateRow(PDOModel.PDO_INSTANCE_NUM, ""+i);
        out.addRow(pdoInstanceNum);

        String pdoModifierCd = generateRow(PDOModel.PDO_MODIFIER_CD, "@");
        out.addRow(pdoModifierCd);

        if (mapConceptCodeType.containsKey(link)) {
            String pdoValueTypeCd = null;
            String type = mapConceptCodeType.get(link);
            if (isNumericType(type)) {
                pdoValueTypeCd = generateRow(PDOModel.PDO_VALUETYPE_CD, "N");
            } else {
                pdoValueTypeCd = generateRow(PDOModel.PDO_VALUETYPE_CD, "T");
            }
            out.addRow(pdoValueTypeCd);
            addValuesPdo(answer, type, out);

            String realLink = link;

            if (!isRawConceptCD(type)) {
                realLink = getRealLink(link, answer, type);
            }

            if (mapConceptCode.containsKey(realLink)) {
                conceptCd = mapConceptCode.get(realLink);
            } else {
                conceptCd = realLink;
                log.warn("Link: " + realLink + " does not have a correspondence concept_cd. Using: " + realLink +
                        " as concept_cd");
            }
            if (conceptCd.length() > 50) {
                conceptCd = conceptCd.substring(0, 50);
                log.warn("Concept_cd is longer than 50 characters. Trimming to: " + conceptCd + " to continue");
            }
            pdoConceptCd = generateRow(PDOModel.PDO_CONCEPT_CD, conceptCd);
        } else {
            log.warn("link " + link + " does not map to any i2b2 concept_cd. Continue using NO_CONCEPT_CD as " +
                    "concept_cd");
            pdoConceptCd = generateRow(PDOModel.PDO_CONCEPT_CD, "NO_CONCEPT_CD");
        }

        out.addRow(pdoConceptCd);
        return out;

    }

    private String getRealLink(String link, QuestionnaireResponse.GroupQuestionAnswer answer, String type) {
        String out = link;
        if (type.equals(FHIR_TAG_VALUE_CODING)) {
            CodingDt cdt = (CodingDt) answer.getValue();
            out = out + "_" + cdt.getCode();
        } else if(type.equals(FHIR_TAG_VALUE_BOOLEAN)) {
            IDatatype data = answer.getValue();
            BooleanDt valueBool = (BooleanDt) data;
            if (valueBool.getValue()) {
                out = out + "_Y";
            } else {
                out = out + "_N";
            }
        }

        return out;
    }

    private void addValuesPdo(QuestionnaireResponse.GroupQuestionAnswer answer, String type, Element observation) {
        IDatatype data = answer.getValue();
        if (type.equals(FHIR_TAG_VALUE_QUANTITY))  {
            QuantityDt qdt = (QuantityDt) data;
            BigDecimal value = qdt.getValue();
            String units = qdt.getUnit();
            System.out.println(value + ", " + units);
            String pdoNValNum = generateRow(PDOModel.PDO_NVAL_NUM, "" + value);
            observation.addRow(pdoNValNum);

            if (units!=null) {
                if (!units.isEmpty()) {
                    String pdoUnits = generateRow(PDOModel.PDO_UNITS_CD, units);
                    observation.addRow(pdoUnits);
                }
            }
        } else if(type.equals(FHIR_TAG_VALUE_STRING)) {
            String value = data.toString();
            String pdoTValChar = generateRow(PDOModel.PDO_TVAL_CHAR, value);
            observation.addRow(pdoTValChar);

        } else if(type.equals(FHIR_TAG_VALUE_INTEGER)) {
            IntegerDt valueInt = (IntegerDt) data;
            String value = valueInt.getValueAsString();
            String pdoNValNum = generateRow(PDOModel.PDO_NVAL_NUM, value);
            observation.addRow(pdoNValNum);

        } else if(type.equals(FHIR_TAG_VALUE_BOOLEAN)) {
            BooleanDt valueBool = (BooleanDt) data;
            String value = "Y";
            if (!valueBool.getValue()) {
                value="N";
            }
            String pdoTValChar = generateRow(PDOModel.PDO_TVAL_CHAR, value);
            observation.addRow(pdoTValChar);

        } else if (type.equals(FHIR_TAG_VALUE_DECIMAL)) {
            DecimalDt valueDec = (DecimalDt) data;
            String value = valueDec.getValueAsString();
            String pdoNValNum = generateRow(PDOModel.PDO_NVAL_NUM, value);
            observation.addRow(pdoNValNum);

        } else if (type.equals(FHIR_TAG_VALUE_CODING)) {
            String value = data.toString();
            if (value!=null) {
                String pdoTValChar = generateRow(PDOModel.PDO_TVAL_CHAR, value);
                observation.addRow(pdoTValChar);
            }
        }
    }

}
