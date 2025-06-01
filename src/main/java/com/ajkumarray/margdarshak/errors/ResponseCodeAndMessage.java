package com.ajkumarray.margdarshak.errors;

public class ResponseCodeAndMessage {

    private ResponseCodeAndMessage() {
        throw new UnsupportedOperationException("This is a ResponseCodeAndMessage class and cannot be instantiated");
    }

    public static final String URL_CREATION_SUCCESS = "001";
    public static final String URL_CREATION_FAILED = "002";

}
