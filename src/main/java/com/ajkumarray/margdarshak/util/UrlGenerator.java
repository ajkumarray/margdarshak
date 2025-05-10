package com.ajkumarray.margdarshak.util;

import java.net.URL;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ajkumarray.margdarshak.constants.UrlConstants;
import com.ajkumarray.margdarshak.exception.InvalidUrlException;

/**
 * Utility class for generating and validating short URLs.
 */
@Component
public final class UrlGenerator {
    private static final Random RANDOM = new Random();

    @Value("${url.shortener.base-url:http://localhost:8080/api/v1/urls/}")
    private String baseUrl;

    private UrlGenerator() {
        // Prevent instantiation
    }

    /**
     * Generates a random short code of specified length.
     *
     * @return A randomly generated short code string
     */
    public static String generateShortCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < UrlConstants.SHORT_URL_LENGTH; i++) {
            sb.append(UrlConstants.SHORT_URL_CHARACTERS.charAt(
                RANDOM.nextInt(UrlConstants.SHORT_URL_CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * Generates a complete short URL by combining base URL and short code.
     *
     * @return A complete short URL that can be used for redirection
     */
    public String generateShortUrl() {
        String shortCode = generateShortCode();
        return baseUrl + shortCode;
    }

    /**
     * Checks if a URL is valid.
     *
     * @param url The URL to validate
     * @return true if the URL is valid, false otherwise
     */
    public static boolean isValidUrl(String url) {
        try {
            validateUrl(url);
            return true;
        } catch (InvalidUrlException e) {
            return false;
        }
    }

    /**
     * Checks if expiration days are valid.
     *
     * @param days The number of days to validate
     * @return true if the days are valid, false otherwise
     */
    public static boolean isValidExpirationDays(Integer days) {
        try {
            validateExpirationDays(days);
            return true;
        } catch (InvalidUrlException e) {
            return false;
        }
    }

    /**
     * Validates if a URL matches the required pattern and is properly formatted.
     *
     * @param url The URL to validate
     * @throws InvalidUrlException if the URL is invalid
     */
    public static void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new InvalidUrlException("URL cannot be empty");
        }

        // Check URL format
        if (!url.matches(UrlConstants.URL_PATTERN)) {
            throw new InvalidUrlException("Invalid URL format");
        }

        try {
            // Validate URL structure
            new URL(url).toURI();
        } catch (Exception e) {
            throw new InvalidUrlException("Invalid URL structure: " + e.getMessage());
        }

        // Check for malicious patterns
        if (url.toLowerCase().contains("javascript:") || 
            url.toLowerCase().contains("data:") ||
            url.toLowerCase().contains("vbscript:")) {
            throw new InvalidUrlException("Invalid URL scheme");
        }

        // Check URL length
        if (url.length() > 2048) {
            throw new InvalidUrlException("URL too long");
        }
    }

    /**
     * Validates if the expiration days are within acceptable range.
     *
     * @param days The number of days to validate
     * @throws InvalidUrlException if the days are invalid
     */
    public static void validateExpirationDays(Integer days) {
        if (days == null) {
            throw new InvalidUrlException("Expiration days cannot be null");
        }
        
        if (days < UrlConstants.MIN_EXPIRATION_DAYS || 
            days > UrlConstants.MAX_EXPIRATION_DAYS) {
            throw new InvalidUrlException(
                String.format("Expiration days must be between %d and %d",
                    UrlConstants.MIN_EXPIRATION_DAYS,
                    UrlConstants.MAX_EXPIRATION_DAYS));
        }
    }
} 