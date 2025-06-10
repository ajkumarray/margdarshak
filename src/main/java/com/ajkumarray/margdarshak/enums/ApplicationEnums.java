package com.ajkumarray.margdarshak.enums;

import com.ajkumarray.margdarshak.errors.ResponseCodeAndMessage;

public enum ApplicationEnums {

    URL_CREATION_SUCCESS(ResponseCodeAndMessage.URL_CREATION_SUCCESS),
    URL_CREATION_FAILED(ResponseCodeAndMessage.URL_CREATION_FAILED),
    URL_SUCCESS_CODE(ResponseCodeAndMessage.SUCCESS_CODE), 
    URL_FAILED_CODE(ResponseCodeAndMessage.FAILED_CODE),
    INVALID_URL_CODE(ResponseCodeAndMessage.INVALID_URL_CODE),

    
    SIGNUP_SUCCESS(ResponseCodeAndMessage.SIGNUP_SUCCESS),
    SIGNUP_FAILED(ResponseCodeAndMessage.SIGNUP_FAILED),
    LOGIN_SUCCESS(ResponseCodeAndMessage.LOGIN_SUCCESS),
    LOGIN_FAILED(ResponseCodeAndMessage.LOGIN_FAILED),
    USER_ALREADY_EXISTS(ResponseCodeAndMessage.USER_ALREADY_EXISTS),
    INVALID_TOKEN(ResponseCodeAndMessage.INVALID_TOKEN),
    INVALID_HEADER_REQUEST(ResponseCodeAndMessage.INVALID_HEADER_REQUEST);

    private String message;

    private String code;

    ApplicationEnums(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        String finalCode = "M01".concat(code);
        return finalCode;
    }
}
