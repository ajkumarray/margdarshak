package com.ajkumarray.margdarshak.errors;

public class ResponseCodeAndMessage {

    private ResponseCodeAndMessage() {
        throw new UnsupportedOperationException("This is a ResponseCodeAndMessage class and cannot be instantiated");
    }

    public static final String SUCCESS_CODE = "101";
    public static final String FAILED_CODE = "102";
    public static final String INVALID_URL_CODE = "103";
    public static final String URL_CREATION_SUCCESS = "105";
    public static final String URL_CREATION_FAILED = "106";

    public static final String SIGNUP_SUCCESS = "201";
    public static final String SIGNUP_FAILED = "202";
    public static final String LOGIN_SUCCESS = "203";
    public static final String LOGIN_FAILED = "204";
    public static final String USER_ALREADY_EXISTS = "205";
    public static final String INVALID_TOKEN = "206";
}
