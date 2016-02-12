package org.bch.fhir.i2b2.exception;

/**
 * Exception class raised by c3pro-consumer system
 * @author CHIP-IHL
 */
public class FHIRI2B2Exception extends Exception {
    private Exception innerException;

    /**
     * Constructor method. Creates a new exception with a message
     * @param msg The message
     */
    public FHIRI2B2Exception(String msg) {
        super(msg);
    }

    /**
     * Constructor method. It allows to embed another exception
     * @param msg The message
     * @param e The embedded exception
     */
    public FHIRI2B2Exception(String msg, Exception e) {
        super(msg);
        this.innerException = e;
    }

    /**
     * Returns the embedded exception
     * @return The exception
     */
    public Exception getInnerException() {
        return innerException;
    }
}
