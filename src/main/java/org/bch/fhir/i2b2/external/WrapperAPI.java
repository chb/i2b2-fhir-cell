package org.bch.fhir.i2b2.external;

import org.bch.fhir.i2b2.util.HttpRequest;
import org.bch.fhir.i2b2.util.SoapRequest;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by CH176656 on 3/23/2015.
 */
public class WrapperAPI {

    @Inject
    private HttpRequest httpRequest;

    @Inject
    private SoapRequest soapRequest;

    private Logger LOG;

    protected void log(Level level, String message) {
        if (this.LOG == null) {
            this.LOG = Logger.getAnonymousLogger();
        }
        this.LOG.log(level, message);
    }

    public HttpRequest getHttpRequest() {
        if (this.httpRequest==null) {
            this.httpRequest=new HttpRequest();
        }
        return this.httpRequest;
    }

    public SoapRequest getSoapRequest() {
        if (this.soapRequest==null) {
            this.soapRequest = new SoapRequest();
        }
        return this.soapRequest;
    }
    //**************************
    // For testing purposes only
    //**************************
    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }
    public void setSoapRequest(SoapRequest soapRequest) { this.soapRequest = soapRequest;}

}
