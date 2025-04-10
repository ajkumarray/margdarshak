package com.ajkumarray.margdarshak.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Utility class for creating standardized API responses.
 */
public final class ResponseUtil {
    private ResponseUtil() {
        // Prevent instantiation
    }

    /**
     * Creates a success response with the given data.
     *
     * @param data The data to include in the response
     * @return ResponseEntity with the success response
     */
    public static <T> ResponseEntity<T> success(T data) {
        return ResponseEntity.ok(data);
    }

    /**
     * Creates an error response with the given message and status code.
     *
     * @param message The error message
     * @param status The HTTP status code
     * @return ResponseEntity with the error response
     */
    public static ResponseEntity<Map<String, String>> error(String message, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Creates a not found response with the given message.
     *
     * @param message The not found message
     * @return ResponseEntity with the not found response
     */
    public static ResponseEntity<Map<String, String>> notFound(String message) {
        return error(message, HttpStatus.NOT_FOUND);
    }

    /**
     * Creates a bad request response with the given message.
     *
     * @param message The bad request message
     * @return ResponseEntity with the bad request response
     */
    public static ResponseEntity<Map<String, String>> badRequest(String message) {
        return error(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Creates an internal server error response with the given message.
     *
     * @param message The error message
     * @return ResponseEntity with the internal server error response
     */
    public static ResponseEntity<Map<String, String>> internalServerError(String message) {
        return error(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates a response with validation errors.
     *
     * @param errors Map of field names to error messages
     * @return ResponseEntity with the validation errors
     */
    public static ResponseEntity<Map<String, String>> validationError(Map<String, String> errors) {
        return ResponseEntity.badRequest().body(errors);
    }
} 