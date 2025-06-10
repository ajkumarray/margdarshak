package com.ajkumarray.margdarshak.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.exception.ApplicationException;
import com.ajkumarray.margdarshak.models.response.ErrorListResponse;
import com.ajkumarray.margdarshak.models.response.ErrorResponse;

@ControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public final ResponseEntity<ErrorListResponse> applicationException(ApplicationException ex) {
        ErrorListResponse response = new ErrorListResponse();
        ErrorResponse errorResponse = new ErrorResponse();

        errorResponse.setErrorCode(ex.getErrorCode());
        errorResponse.setErrorMessage(ex.getMessage());
        response.setMessage(ex.getMessage());
        response.setMessageCode(ex.getErrorCode());

        response.setError(errorResponse);

        HttpStatus statusCode = HttpStatus.BAD_REQUEST;
        if (ApplicationEnums.LOGIN_FAILED.getCode().equals(ex.getErrorCode())) {
            statusCode = HttpStatus.UNAUTHORIZED;
        }
        if (ApplicationEnums.INVALID_TOKEN.getCode().equals(ex.getErrorCode())) {
            statusCode = HttpStatus.UNAUTHORIZED;
        }
        if (ApplicationEnums.INVALID_HEADER_REQUEST.getCode().equals(ex.getErrorCode())) {
            statusCode = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(response, statusCode);
    }

}
