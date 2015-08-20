package org.bch.fhir.i2b2.service;

import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.composite.ContainedDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.QuestionnaireAnswers;
import ca.uhn.fhir.model.primitive.IntegerDt;
import org.bch.fhir.i2b2.config.AppConfig;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.bch.fhir.i2b2.pdomodel.Element;
import org.bch.fhir.i2b2.pdomodel.ElementSet;
import org.bch.fhir.i2b2.pdomodel.PDOModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: CH176656
 * Date: 7/27/15
 * Time: 8:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class ObservationToI2B2 extends FHIRToPDO {
    Logger log = LoggerFactory.getLogger(ObservationToI2B2.class);


    @Override
    public String getPDOXML(BaseResource resource) throws FHIRI2B2Exception {
        Observation obs = (Observation) resource;
        PDOModel pdo = new PDOModel();
        if (obs!=null) {
            this.patientIde = this.getPatientId(obs);
            Encounter enc = findEncounter(obs);
            this.eventIde = this.getEventId(enc);
            ElementSet eidSet = this.generateEIDSet();
            ElementSet pidSet = this.generatePIDSet();
            ElementSet eventSet = this.generateEventSet(enc);
            ElementSet patientSet = this.generatePatientSet();
            ElementSet observationSet = this.generateObservationSet(obs);
            pdo.addElementSet(eidSet);
            pdo.addElementSet(pidSet);
            pdo.addElementSet(eventSet);
            pdo.addElementSet(patientSet);
            pdo.addElementSet(observationSet);

            // We add metadata for admin purposes
            addMetadataInObservationSet("Observation", METADATA_CONCEPT_CD, observationSet);
        }
        return pdo.generatePDOXML();
    }

    private String getPatientId(Observation obs) throws FHIRI2B2Exception {
        ResourceReferenceDt refPatient = obs.getSubject();
        if (refPatient.isEmpty()) throw new FHIRI2B2Exception("Subject reference is not informed");
        String idPat = refPatient.getReference().getIdPart();
        return idPat;
    }

    private Encounter findEncounter(Observation obs) throws FHIRI2B2Exception {
        ResourceReferenceDt refEncounter = obs.getEncounter();
        if (!refEncounter.isEmpty()) {
            String idEnc = refEncounter.getReference().getIdPart();
            ContainedDt containedDt = obs.getContained();
            List<IResource> iResources = containedDt.getContainedResources();
            IResource encRes = findResourceById(iResources, idEnc);
            if (encRes == null) {
                log.warn("Encounter reference not found in contained list. A random encounter number will be generate");
            } else {
                Encounter enc = (Encounter) encRes;
                return enc;
            }
        } else {
            log.warn("Encounter reference not found in contained list. A random encounter number will be generate");
        }

        Encounter enc = new Encounter();
        return enc;
    }

    private ElementSet generateObservationSet(Observation obs) throws FHIRI2B2Exception {
        ElementSet observationSet = new ElementSet();
        observationSet.setTypePDOSet(ElementSet.PDO_OBSERVATION_SET);

        Element out = new Element();
        out.setTypePDO(Element.PDO_OBSERVATION);
        Map<String, String> mapConceptCode = AppConfig.getRealConceptCodesObsMap();
        Map<String, String> mapConceptCodeType = AppConfig.getRealConceptCodesTypeObsMap();

        System.out.println(mapConceptCodeType.toString());
        System.out.println(mapConceptCode.toString());

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

        String pdoInstanceNum = generateRow(PDOModel.PDO_INSTANCE_NUM, ""+1);
        out.addRow(pdoInstanceNum);

        String pdoModifierCd = generateRow(PDOModel.PDO_MODIFIER_CD, "@");
        out.addRow(pdoModifierCd);

        if (obs.getCode().getCoding().isEmpty())
            throw new FHIRI2B2Exception("Coding is not informed in the Observation");

        // We just get the first coding system.
        // POSSIBLE TODO: check all coding systems until we find one that have a valid mapping
        CodingDt codingDt = obs.getCode().getCoding().get(0);

        String systemCode = codingDt.getSystem();
        String codeCode = codingDt.getCode();

        String codeToLookFor = systemCode + "/" + codeCode;

        System.out.println(codeToLookFor);
        String pdoValueTypeCd = null;
        String type = mapConceptCodeType.get(codeToLookFor);
        if (isNumericType(type)) {
            pdoValueTypeCd = generateRow(PDOModel.PDO_VALUETYPE_CD, "N");
        } else {
            pdoValueTypeCd = generateRow(PDOModel.PDO_VALUETYPE_CD, "T");
        }
        out.addRow(pdoValueTypeCd);

        addValuesPdo(obs, type, out);
        if (mapConceptCode.containsKey(codeToLookFor)) {
            conceptCd = mapConceptCode.get(codeToLookFor);
        } else {
            conceptCd = codeToLookFor;
            log.warn("Code: " + codeToLookFor + " does not have a correspondence concept_cd. Using: " + codeToLookFor +
                    " as concept_cd");
        }
        if (conceptCd.length() > 50) {
            conceptCd = conceptCd.substring(0, 50);
            log.warn("Concept_cd is longer than 50 characters. Trimming to: " + conceptCd + " to continue");
        }
        pdoConceptCd = generateRow(PDOModel.PDO_CONCEPT_CD, conceptCd);
        out.addRow(pdoConceptCd);

        observationSet.addElement(out);
        return observationSet;

    }

    private void addValuesPdo(Observation obs, String type, Element out) {
        IDatatype data = obs.getValue();
        if (type == null) type = FHIR_TAG_VALUE_STRING;
        if (type.equals(FHIR_TAG_VALUE_QUANTITY))  {
            QuantityDt qdt = (QuantityDt) data;
            BigDecimal value = qdt.getValue();
            String units = qdt.getUnits();
            System.out.println(value + ", " + units);
            String pdoNValNum = generateRow(PDOModel.PDO_NVAL_NUM, ""+value);
            out.addRow(pdoNValNum);

            if (units!=null) {
                if (!units.isEmpty()) {
                    String pdoUnits = generateRow(PDOModel.PDO_UNITS_CD, units);
                    out.addRow(pdoUnits);
                }
            }
        } else if(type.equals(FHIR_TAG_VALUE_STRING)) {
            String value = data.toString();
            String pdoTValChar = generateRow(PDOModel.PDO_TVAL_CHAR, value);
            out.addRow(pdoTValChar);
        } else if(type.equals(FHIR_TAG_VALUE_INTEGER)) {
            IntegerDt valueInt = (IntegerDt) data;
            String value = valueInt.getValueAsString();
            String pdoNValNum = generateRow(PDOModel.PDO_NVAL_NUM, value);
            out.addRow(pdoNValNum);
        }
    }
}
