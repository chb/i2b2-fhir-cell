package org.bch.fhir.i2b2.util;

/**
 * Interface for http responses
 * Created by CH176656 on 3/26/2015.
 */
public interface Response {
    public int getResponseCode();
    public String getContent();
}
