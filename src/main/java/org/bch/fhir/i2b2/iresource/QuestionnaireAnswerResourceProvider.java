package org.bch.fhir.i2b2.iresource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.dstu2.resource.QuestionnaireAnswers;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;
import org.bch.fhir.i2b2.external.I2B2CellFR;
import org.bch.fhir.i2b2.service.FHIRToPDO;
import org.bch.fhir.i2b2.service.QAnswersToI2B2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

/**
 * Created by CH176656 on 4/30/2015.
 */
public class QuestionnaireAnswerResourceProvider implements IResourceProvider  {

    Logger log = LoggerFactory.getLogger(QuestionnaireAnswerResourceProvider.class);

    protected FhirContext ctx = FhirContext.forDstu2();

    protected FHIRToPDO mapper = new QAnswersToI2B2();
    protected I2B2CellFR i2b2 = new I2B2CellFR();

    @Override
    public Class<QuestionnaireAnswers> getResourceType() {
        return QuestionnaireAnswers.class;
    }

    @Create()
    public MethodOutcome createQA(@ResourceParam QuestionnaireAnswers theQA) {
        log.info("New POST QuestionnaireAnswers");

        String xmlpdo = null;
        try {
            xmlpdo = mapper.getPDOXML(theQA);
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
/*
    private void addNewVersion(QuestionnaireAnswers theQA, String theId) {
        InstantDt publishedDate;
        if (!myIdToQVersions.containsKey(theId)) {
            myIdToQVersions.put(theId, new LinkedList<QuestionnaireAnswers>());
            publishedDate = InstantDt.withCurrentTime();
        } else {
            QuestionnaireAnswers currentQA = myIdToQVersions.get(theId).getLast();
            Map<ResourceMetadataKeyEnum<?>, Object> resourceMetadata = currentQA.getResourceMetadata();
            publishedDate = (InstantDt) resourceMetadata.get(ResourceMetadataKeyEnum.PUBLISHED);
        }

      theQA.getResourceMetadata().put(ResourceMetadataKeyEnum.PUBLISHED, publishedDate);
        theQA.getResourceMetadata().put(ResourceMetadataKeyEnum.UPDATED, InstantDt.withCurrentTime());

        Deque<QuestionnaireAnswers> existingVersions = myIdToQVersions.get(theId);

        // We just use the current number of versions as the next version number
        String newVersion = Integer.toString(existingVersions.size());

        // Create an ID with the new version and assign it back to the resource
        IdDt newId = new IdDt("QuestionnaireAnswers", theId, newVersion);
        theQA.setId(newId);
        existingVersions.add(theQA);
    }
*/
}
