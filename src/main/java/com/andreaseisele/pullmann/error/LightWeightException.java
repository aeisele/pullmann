package com.andreaseisele.pullmann.error;

/**
 * Exception where we are not interested in the stack trace.
 */
public class LightWeightException extends RuntimeException {

    public LightWeightException(String message) {
        super(message);
    }

    public LightWeightException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}

