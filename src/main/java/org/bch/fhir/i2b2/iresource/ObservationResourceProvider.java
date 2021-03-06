package org.bch.fhir.i2b2.iresource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.bch.fhir.i2b2.external.I2B2CellFR;
import org.bch.fhir.i2b2.service.FHIRToPDO;
import org.bch.fhir.i2b2.service.ObservationToI2B2;

import java.io.IOException;

/**
 * The observation resource provider class
 * @author CHIP-IHL
 */
public class ObservationResourceProvider implements IResourceProvider {
    Log log = LogFactory.getLog(ObservationResourceProvider.class);

    protected FhirContext ctx = FhirContext.forDstu2();

    protected FHIRToPDO mapper = new ObservationToI2B2();
    protected I2B2CellFR i2b2 = new I2B2CellFR();

    /**
     * Returns the resource type: Observation
     * @return The type
     */
    @Override
    public Class<Observation> getResourceType() {
        return Observation.class;
    }

    /**
     * Create Observation POST handle
     * @param obs The observation
     * @return
     */
    @Create()
    public MethodOutcome createQA(@ResourceParam Observation obs) {
        log.info("New POST Observation");

        String xmlpdo = null;
        try {
            xmlpdo = mapper.getPDOXML(obs);
            i2b2.pushPDOXML(xmlpdo);
        } catch (FHIRI2B2Exception e) {
            // We return 500!
            log.error("Error POST QuestionnaireAnswers:" + e.getMessage());
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        } catch (IOException e) {
            log.error("Error POST QuestionnaireAnswers IOException:" + e.getMessage());
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }

        return new MethodOutcome();
    }

}
