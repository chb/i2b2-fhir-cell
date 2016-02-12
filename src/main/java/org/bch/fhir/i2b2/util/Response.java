package org.bch.fhir.i2b2.util;

/**
 * Interface for http responses
 * @author CHIP-IHL
 */
public interface Response {
    public int getResponseCode();
    public String getContent();
}
