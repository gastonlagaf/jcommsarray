package com.jcommsarray.client.ice.exception;

public class IceFailureException extends RuntimeException {

    public IceFailureException(String message) {
        super(message);
    }

    public IceFailureException(String message, Throwable cause) {
        super(message, cause);
    }

}
