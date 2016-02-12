package org.bch.fhir.i2b2.iresource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.QuestionnaireResponse;
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
import org.bch.fhir.i2b2.service.QResponseToI2B2;

import java.io.IOException;

/**
 * QustionnaireResponse resource provider class
 * @author CHIP-IHL
 */
public class QuestionnaireResponseResourceProvider implements IResourceProvider {
    Log log = LogFactory.getLog(QuestionnaireResponseResourceProvider.class);

    protected FhirContext ctx = FhirContext.forDstu2();

    protected FHIRToPDO mapper = new QResponseToI2B2();
    protected I2B2CellFR i2b2 = new I2B2CellFR();

    /**
     * Returns the resource type: QuestionnaireResponse
     * @return The type
     */
    @Override
    public Class<QuestionnaireResponse> getResourceType() {
        return QuestionnaireResponse.class;
    }

    /**
     * The QuestionnaireResponse POST handle
     * @param theQR The QuestionnaireResponse
     * @return
     */
    @Create()
    public MethodOutcome createQA(@ResourceParam QuestionnaireResponse theQR) {
        log.info("New POST QuestionnaireResponse");

        String xmlpdo = null;
        try {
            xmlpdo = mapper.getPDOXML(theQR);
            i2b2.pushPDOXML(xmlpdo);
        } catch (FHIRI2B2Exception e) {
            // We return 500!
            log.error("Error POST QuestionnaireResponse:" + e.getMessage());
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        } catch (IOException e) {
            log.error("Error POST QuestionnaireResponse IOException:" + e.getMessage());
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }

        return new MethodOutcome();
    }
}
