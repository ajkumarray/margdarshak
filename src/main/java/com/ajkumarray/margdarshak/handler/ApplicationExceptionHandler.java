package com.ajkumarray.margdarshak.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.exception.ApplicationException;
import com.ajkumarray.margdarshak.models.response.ErrorListResponse;
import com.ajkumarray.margdarshak.models.response.ErrorResponse;
import com.ajkumarray.margdarshak.util.MessageTranslator;

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
        if (ApplicationEnums.CUSTOM_CODE_TAKEN.getCode().equals(ex.getErrorCode())) {
            statusCode = HttpStatus.CONFLICT;
        }
        return new ResponseEntity<>(response, statusCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final ResponseEntity<ErrorListResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(MessageTranslator.toLocale(ApplicationEnums.FAILED_MESSAGE.getCode()));

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(ApplicationEnums.FAILED_MESSAGE.getCode());
        errorResponse.setErrorMessage(message);

        ErrorListResponse response = new ErrorListResponse();
        response.setMessage(message);
        response.setMessageCode(ApplicationEnums.FAILED_MESSAGE.getCode());
        response.setError(errorResponse);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
