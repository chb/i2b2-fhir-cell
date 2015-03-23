package org.bch.i2me2.core.external;

import org.bch.i2me2.core.util.HttpRequest;

import javax.inject.Inject;

/**
 * Created by CH176656 on 3/23/2015.
 */
public class WrapperAPI {

    @Inject
    protected HttpRequest http;


    //**************************
    // For testing purposes only
    //**************************
    public void setHttp(HttpRequest http) {
        this.http = http;
    }
}
