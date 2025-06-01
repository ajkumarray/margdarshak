package com.ajkumarray.margdarshak.enums;

import com.ajkumarray.margdarshak.errors.ResponseCodeAndMessage;

public enum ApplicationEnums {

    URL_CREATION_SUCCESS(ResponseCodeAndMessage.URL_CREATION_SUCCESS),
    URL_CREATION_FAILED(ResponseCodeAndMessage.URL_CREATION_FAILED);

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
