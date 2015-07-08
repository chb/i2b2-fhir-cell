package org.bch.fhir.core.exception;

/**
 * I2ME2Exception
 * Created by CH176656 on 3/10/2015.
 */
public class I2ME2Exception extends Exception {

    private Exception innerException;
    public I2ME2Exception(String msg) {
        super(msg);
    }

    public I2ME2Exception(String msg, Exception e) {
        super(msg);
        this.innerException = e;
    }

    public Exception getInnerException() {
        return innerException;
    }

}
