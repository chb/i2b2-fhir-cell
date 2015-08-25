package org.bch.fhir.i2b2.service;

import ca.uhn.fhir.model.dstu2.composite.AddressDt;
import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.bch.fhir.i2b2.pdomodel.Element;
import org.bch.fhir.i2b2.pdomodel.ElementSet;
import org.bch.fhir.i2b2.pdomodel.PDOModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by ipinyol on 8/25/15.
 */
public class PatientToI2B2 extends FHIRToPDO {
    Logger log = LoggerFactory.getLogger(PatientToI2B2.class);
    public static final String SEP = "##";

    @Override
    public String getPDOXML(BaseResource resource) throws FHIRI2B2Exception {
        Patient patient = (Patient) resource;
        PDOModel pdo = new PDOModel();
        if (patient!=null) {
            this.patientIde = this.getPatiendIde(patient);
            ElementSet patientSet = this.generatePatientSet(patient);
            pdo.addElementSet(patientSet);
            // If patientSet is null, nothing to update
            if (patientSet == null) return null;
            ElementSet pidSet = this.generatePIDSet();
            pdo.addElementSet(pidSet);
        }

        return pdo.generatePDOXML();
    }
    // return null if bot zip code and state are not present
    protected ElementSet generatePatientSet(Patient patient) throws FHIRI2B2Exception {
        //<param column="sex_cd">F__PatientSegments.patientGender__F</param>
        ElementSet patientSet = new ElementSet();
        patientSet.setTypePDOSet(ElementSet.PDO_PATIENT_SET);
        Element patientElement = new Element();
        patientElement.setTypePDO(Element.PDO_PATIENT);

        String pdoPatientId = this.generateRow(PDOModel.PDO_PATIENT_ID, this.patientIde,
                genParamStr(PDOModel.PDO_SOURCE, this.patientIdeSource));
        patientElement.addRow(pdoPatientId);

        String address = this.getAddressInfo(patient);
        String zip = address.split(SEP)[0];
        String state = address.split(SEP)[1];
        String zipElement = this.generateRow(PDOModel.PDO_PARAM, zip,
                genParamStr(PDOModel.PDO_COLUMN, PDOModel.PDO_COLUMN_ZIP_CD),
                genParamStr(PDOModel.PDO_TYPE, "string"));
        
        String stateElement = this.generateRow(PDOModel.PDO_PARAM, state,
                genParamStr(PDOModel.PDO_COLUMN, PDOModel.PDO_COLUMN_STATE_PATH),
                genParamStr(PDOModel.PDO_TYPE, "string"));

        boolean isInfoPresent=false;
        if (zip.length()!=0) {
            patientElement.addRow(zipElement);
            isInfoPresent = true;
        }
        if (state.length()!=0) {
            patientElement.addRow(stateElement);
            isInfoPresent = true;
        }
        if (!isInfoPresent) return null;

        patientSet.addElement(patientElement);
        return patientSet;
    }

    // PRE: patient is not null
    private String getAddressInfo(Patient patient) {
        List<AddressDt> addrs = patient.getAddress();
        if (addrs.isEmpty()) {
            log.warn("No address information is provided for the Patient resource");
            return SEP;
        }
        AddressDt d = addrs.get(0);
        String zip = d.getPostalCode();
        String state = d.getState();

        return zip+SEP+state;
    }

    // PRE: patient is not null
    private String getPatiendIde(Patient patient) {
        return patient.getId().getIdPart();
    }
}
