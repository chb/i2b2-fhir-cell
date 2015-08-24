package org.bch.fhir.i2b2.service;

import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.*;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Questionnaire;
import ca.uhn.fhir.model.dstu2.resource.QuestionnaireAnswers;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DecimalDt;
import ca.uhn.fhir.model.primitive.IntegerDt;
import org.bch.fhir.i2b2.config.AppConfig;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.bch.fhir.i2b2.pdomodel.Element;
import org.bch.fhir.i2b2.pdomodel.ElementSet;
import org.bch.fhir.i2b2.pdomodel.PDOModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.message.callback.PrivateKeyCallback;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Converts a QuestionnaireAnswer FHIR resource to the corresponding XMLPDO
 * Created by ipinyol on 7/9/15.
 */
public class QAnswersToI2B2 extends FHIRToPDO {

    Logger log = LoggerFactory.getLogger(QAnswersToI2B2.class);


    @Override
    public String getPDOXML(BaseResource resource) throws FHIRI2B2Exception {
        QuestionnaireAnswers qa = (QuestionnaireAnswers) resource;
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

    private String getQReference(QuestionnaireAnswers qa) {
        ResourceReferenceDt questionnaireRef = qa.getQuestionnaire();
        if (questionnaireRef!=null) {
            return questionnaireRef.getReference().getIdPart();
        } else {
            QuestionnaireAnswers.Group gr = qa.getGroup();
            if (gr != null) {
                return qa.getGroup().getLinkId();
            }
        }
        return "";
    }

    private Encounter findEncounter(QuestionnaireAnswers qa) throws FHIRI2B2Exception{
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

    private String getPatientId(QuestionnaireAnswers qa) throws FHIRI2B2Exception {
        ResourceReferenceDt refPatient = qa.getSubject();
        if (refPatient.isEmpty()) throw new FHIRI2B2Exception("Subject reference is not informed");
        String idPat = refPatient.getReference().getIdPart();
        return idPat;
    }



    private ElementSet generateObservationSet(QuestionnaireAnswers qa) throws FHIRI2B2Exception {
        ElementSet observationSet = new ElementSet();
        observationSet.setTypePDOSet(ElementSet.PDO_OBSERVATION_SET);

        QuestionnaireAnswers.Group group = qa.getGroup();

        processGroup(group, observationSet);
        return observationSet;
    }

    private void processGroup(QuestionnaireAnswers.Group group, ElementSet observationSet) throws FHIRI2B2Exception{
        if (group.getGroup().isEmpty() && group.getQuestion().isEmpty())
            throw new FHIRI2B2Exception("Group does not have any question nor group");

        if (!group.getGroup().isEmpty()) {
            List<QuestionnaireAnswers.Group> groups = group.getGroup();
            for (QuestionnaireAnswers.Group gr : groups) {
                processGroup(gr, observationSet);
            }
        } else {
            List<QuestionnaireAnswers.GroupQuestion> questions = group.getQuestion();
            for (QuestionnaireAnswers.GroupQuestion question: questions) {
                processQuestion(question, observationSet);
            }
        }
    }

    private void processQuestion(QuestionnaireAnswers.GroupQuestion question, ElementSet observationSet)
            throws FHIRI2B2Exception {
        if (!question.getAnswer().isEmpty()) {
            List<QuestionnaireAnswers.GroupQuestionAnswer> answers = question.getAnswer();
            String link = question.getLinkId();
            int i=1;
            for (QuestionnaireAnswers.GroupQuestionAnswer answer: answers) {
                Element observation = generateObservation(answer, i, link);
                observationSet.addElement(observation);
            }
        }

        if (!question.getGroup().isEmpty()) {
            for(QuestionnaireAnswers.Group gr: question.getGroup()) {
                processGroup(gr, observationSet);
            }
        }
    }

    private Element generateObservation(QuestionnaireAnswers.GroupQuestionAnswer answer, int i, String link)
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
            System.out.println("TYPE: " + type);
            if (isNumericType(type)) {
                pdoValueTypeCd = generateRow(PDOModel.PDO_VALUETYPE_CD, "N");
            } else {
                pdoValueTypeCd = generateRow(PDOModel.PDO_VALUETYPE_CD, "T");
            }
            System.out.println(pdoValueTypeCd);
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

    private String getRealLink(String link, QuestionnaireAnswers.GroupQuestionAnswer answer, String type) {
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

    private void addValuesPdo(QuestionnaireAnswers.GroupQuestionAnswer answer, String type, Element observation) {
        IDatatype data = answer.getValue();
        if (type.equals(FHIR_TAG_VALUE_QUANTITY))  {
            QuantityDt qdt = (QuantityDt) data;
            BigDecimal value = qdt.getValue();
            String units = qdt.getUnits();
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
