package com.ajkumarray.margdarshak.enums;

import com.ajkumarray.margdarshak.errors.ResponseCodeAndMessage;

public enum ApplicationEnums {

    URL_CREATION_SUCCESS(ResponseCodeAndMessage.URL_CREATION_SUCCESS),
    URL_CREATION_FAILED(ResponseCodeAndMessage.URL_CREATION_FAILED),
    URL_SUCCESS_CODE(ResponseCodeAndMessage.SUCCESS_CODE), URL_FAILED_CODE(ResponseCodeAndMessage.FAILED_CODE),
    INVALID_URL_CODE(ResponseCodeAndMessage.INVALID_URL_CODE);

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
