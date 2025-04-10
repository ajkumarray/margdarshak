package com.ajkumarray.margdarshak.util;

import java.util.regex.Pattern;

/**
 * Utility class for security-related operations.
 */
public final class SecurityUtil {
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "<script.*?>.*?</script>|<.*?javascript:.*?>|&lt;script.*?&gt;.*?&lt;/script&gt;|&lt;.*?javascript:.*?&gt;",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL
    );

    private SecurityUtil() {
        // Prevent instantiation
    }

    /**
     * Sanitizes input to prevent XSS attacks.
     *
     * @param input The input to sanitize
     * @return Sanitized input
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return XSS_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Validates if a string contains any XSS patterns.
     *
     * @param input The input to validate
     * @return true if the input contains XSS patterns, false otherwise
     */
    public static boolean containsXssPatterns(String input) {
        if (input == null) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }

    /**
     * Validates if a string is a valid short URL.
     *
     * @param shortUrl The short URL to validate
     * @return true if the short URL is valid, false otherwise
     */
    public static boolean isValidShortUrl(String shortUrl) {
        if (shortUrl == null || shortUrl.length() != 6) {
            return false;
        }
        return shortUrl.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * Validates if a string is a valid origin for CORS.
     *
     * @param origin The origin to validate
     * @return true if the origin is valid, false otherwise
     */
    public static boolean isValidOrigin(String origin) {
        if (origin == null) {
            return false;
        }
        return origin.matches("^https?://([a-zA-Z0-9-]+\\.)*[a-zA-Z0-9-]+(:\\d+)?$");
    }
} 