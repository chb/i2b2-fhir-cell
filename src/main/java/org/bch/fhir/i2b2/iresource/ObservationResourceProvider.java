package org.bch.fhir.i2b2.iresource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.QuestionnaireAnswers;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: CH176656
 * Date: 7/24/15
 * Time: 8:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class ObservationResourceProvider implements IResourceProvider {
    Logger log = LoggerFactory.getLogger(ObservationResourceProvider.class);

    protected String generateNewId() {
        return UUID.randomUUID().toString();
    }

    protected FhirContext ctx = FhirContext.forDstu2();

    @Override
    public Class<Observation> getResourceType() {
        return Observation.class;
    }
    @Create()
    public MethodOutcome createQA(@ResourceParam Observation obs) {
        log.info("New POST Observation");

        String xmlpdo = null;
        /*
        try {
            //xmlpdo = mapper.getPDOXML(theQA);
            //i2b2.pushPDOXML(xmlpdo);
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
        */
        return new MethodOutcome();
    }

}
