package com.xingtb.fpe;

public class FPEException extends Exception {
    private static final long serialVersionUID = 1L;

    public FPEException(String message) {
        super(message);
    }


    public FPEException(String message, Throwable cause) {
        super(message, cause);
    }
}
