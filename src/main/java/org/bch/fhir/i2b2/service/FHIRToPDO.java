package org.bch.fhir.i2b2.service;

import ca.uhn.fhir.model.dstu2.resource.BaseResource;
import org.bch.fhir.i2b2.exception.FHIRI2B2Exception;

/**
 * Created with IntelliJ IDEA.
 * User: CH176656
 * Date: 7/22/15
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class FHIRToPDO {
    public abstract String getPDOXML(BaseResource resource) throws FHIRI2B2Exception;

}
