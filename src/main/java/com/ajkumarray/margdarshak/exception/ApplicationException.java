package com.ajkumarray.margdarshak.exception;

public class ApplicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String errorCode;
    private final String message;

    public ApplicationException(String message, String code) {
        super(message);
        this.errorCode = code;
        this.message = message;
    }

    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "";
        this.message = "";
    }

    public ApplicationException(String message) {
        super(message);
        this.errorCode = "";
        this.message = "";
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

}