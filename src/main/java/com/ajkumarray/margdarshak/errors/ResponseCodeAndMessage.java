package com.ajkumarray.margdarshak.errors;

public class ResponseCodeAndMessage {

    private ResponseCodeAndMessage() {
        throw new UnsupportedOperationException("This is a ResponseCodeAndMessage class and cannot be instantiated");
    }

    public static final String URL_CREATION_SUCCESS = "001";
    public static final String URL_CREATION_FAILED = "002";
    public static final String SUCCESS_CODE = "101";
    public static final String FAILED_CODE = "102";
    public static final String INVALID_URL_CODE = "103";
    public static final String USER_REGISTRATION_FAILED = "003";
    public static final String USER_LOGIN_FAILED = "004";
    public static final String USER_ALREADY_EXISTS = "005";
}
