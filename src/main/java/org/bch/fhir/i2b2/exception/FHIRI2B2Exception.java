package org.bch.fhir.i2b2.exception;

/**
 * Created by ipinyol on 7/9/15.
 */
public class FHIRI2B2Exception extends Exception {
    private Exception innerException;
    public FHIRI2B2Exception(String msg) {
        super(msg);
    }

    public FHIRI2B2Exception(String msg, Exception e) {
        super(msg);
        this.innerException = e;
    }

    public Exception getInnerException() {
        return innerException;
    }
}
