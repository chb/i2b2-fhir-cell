package org.bch.fhir.i2b2.iresource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.bch.fhir.i2b2.external.I2B2CellFR;
import org.bch.fhir.i2b2.service.FHIRToPDO;
import org.bch.fhir.i2b2.service.PatientToI2B2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by ipinyol on 8/25/15.
 */
public class PatientResourceProvider implements IResourceProvider {

    protected FhirContext ctx = FhirContext.forDstu2();
    protected FHIRToPDO mapper = new PatientToI2B2();
    protected I2B2CellFR i2b2 = new I2B2CellFR();

    Logger log = LoggerFactory.getLogger(PatientResourceProvider.class);

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    @Update
    public MethodOutcome update(@ResourceParam Patient patient) {
        String xmlpdo = null;
        try {
            xmlpdo = mapper.getPDOXML(patient);
            System.out.println(xmlpdo);
            if (xmlpdo!=null) {
                i2b2.pushPDOXML(xmlpdo);
            } else {
                log.warn("Patient resource has been informed but not data to update i2b2");
            }
        } catch (FHIRI2B2Exception e) {
            // We return 500!
            log.error("Error PUT Patient:" + e.getMessage());
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        } catch (IOException e) {
            log.error("Error PUT Patient IOException:" + e.getMessage());
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }

        return new MethodOutcome();
    }
}
