package com.ajkumarray.margdarshak.constants;

/**
 * Constants related to URL operations and validation.
 */
public final class UrlConstants {
    private UrlConstants() {
        // Prevent instantiation
    }

    /**
     * Characters used for generating short URLs.
     */
    public static final String SHORT_URL_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Length of generated short URLs.
     */
    public static final int SHORT_URL_LENGTH = 6;

    /**
     * Maximum number of retries for generating a unique short URL.
     */
    public static final int MAX_SHORT_URL_GENERATION_RETRIES = 3;

    /**
     * Regular expression pattern for validating URLs.
     */
    public static final String URL_PATTERN = "^https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$";

    /**
     * Minimum number of days for URL expiration.
     */
    public static final int MIN_EXPIRATION_DAYS = 1;

    /**
     * Maximum number of days for URL expiration.
     */
    public static final int MAX_EXPIRATION_DAYS = 365;

    /**
     * Default system user for URL creation.
     */
    public static final String SYSTEM_USER = "system";
} 