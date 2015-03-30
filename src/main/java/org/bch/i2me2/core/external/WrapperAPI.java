package org.bch.i2me2.core.external;

import org.bch.i2me2.core.util.HttpRequest;
import org.bch.i2me2.core.util.SoapRequest;

import javax.inject.Inject;

/**
 * Created by CH176656 on 3/23/2015.
 */
public class WrapperAPI {

    @Inject
    private HttpRequest httpRequest;

    @Inject
    private SoapRequest soapRequest;

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
