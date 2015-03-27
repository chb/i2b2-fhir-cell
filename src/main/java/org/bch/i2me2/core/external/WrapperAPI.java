package org.bch.i2me2.core.external;

import org.bch.i2me2.core.util.HttpRequest;
import org.bch.i2me2.core.util.SoapRequest;

import javax.inject.Inject;

/**
 * Created by CH176656 on 3/23/2015.
 */
public class WrapperAPI {

    @Inject
    protected HttpRequest httpRequest;

    @Inject
    protected SoapRequest soapRequest;

    //**************************
    // For testing purposes only
    //**************************
    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }
    public void setSoapRequest(SoapRequest soapRequest) { this.soapRequest = soapRequest;}

}
