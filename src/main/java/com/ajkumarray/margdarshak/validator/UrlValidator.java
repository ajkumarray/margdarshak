package com.ajkumarray.margdarshak.validator;

import org.springframework.stereotype.Component;

import com.ajkumarray.margdarshak.constants.UrlConstants;
import com.ajkumarray.margdarshak.exception.InvalidUrlException;
import com.ajkumarray.margdarshak.util.UrlGenerator;

/**
 * Validator for URL-related operations.
 * Provides methods to validate URLs and their associated parameters.
 */
@Component
public class UrlValidator {
    /**
     * Validates a URL and its expiration days.
     *
     * @param url The URL to validate
     * @param expirationDays The number of days until expiration
     * @throws InvalidUrlException if the URL is invalid or expiration days are invalid
     */
    public void validateUrl(String url, Integer expirationDays) throws InvalidUrlException {
        if (url == null || url.trim().isEmpty()) {
            throw new InvalidUrlException("URL cannot be empty");
        }

        if (!UrlGenerator.isValidUrl(url)) {
            throw new InvalidUrlException("Invalid URL format");
        }

        if (!UrlGenerator.isValidExpirationDays(expirationDays)) {
            throw new InvalidUrlException("Expiration days must be between " + 
                UrlConstants.MIN_EXPIRATION_DAYS + " and " + 
                UrlConstants.MAX_EXPIRATION_DAYS);
        }
    }
} 