package com.ajkumarray.margdarshak.util;

import java.util.Random;

import com.ajkumarray.margdarshak.constants.UrlConstants;

/**
 * Utility class for generating and validating short URLs.
 */
public final class UrlGenerator {
    private static final Random RANDOM = new Random();

    private UrlGenerator() {
        // Prevent instantiation
    }

    /**
     * Generates a random short URL of specified length.
     *
     * @return A randomly generated short URL string
     */
    public static String generateShortUrl() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < UrlConstants.SHORT_URL_LENGTH; i++) {
            sb.append(UrlConstants.SHORT_URL_CHARACTERS.charAt(
                RANDOM.nextInt(UrlConstants.SHORT_URL_CHARACTERS.length())));
        }
        return sb.toString();
    }

    /**
     * Validates if a URL matches the required pattern.
     *
     * @param url The URL to validate
     * @return true if the URL is valid, false otherwise
     */
    public static boolean isValidUrl(String url) {
        return url != null && url.matches(UrlConstants.URL_PATTERN);
    }

    /**
     * Validates if the expiration days are within acceptable range.
     *
     * @param days The number of days to validate
     * @return true if the days are valid, false otherwise
     */
    public static boolean isValidExpirationDays(Integer days) {
        return days != null && 
               days >= UrlConstants.MIN_EXPIRATION_DAYS && 
               days <= UrlConstants.MAX_EXPIRATION_DAYS;
    }
} 