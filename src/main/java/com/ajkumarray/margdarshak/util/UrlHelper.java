package com.ajkumarray.margdarshak.util;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ajkumarray.margdarshak.constants.UrlConstants;
import com.ajkumarray.margdarshak.entity.UrlMasterEntity;
import com.ajkumarray.margdarshak.enums.UrlStatusEnums;
import com.ajkumarray.margdarshak.exception.ApplicationException;
import com.ajkumarray.margdarshak.models.request.UrlMasterRequest;
import com.ajkumarray.margdarshak.models.response.UrlMasterResponse;
import com.ajkumarray.margdarshak.util.CommonFunctionHelper;

/**
 * Utility class for generating and validating short URLs.
 */
@Component
public final class UrlHelper {

    @Autowired
    private CommonFunctionHelper commonFunctionHelper;

    private static final Random RANDOM = new Random();

    @Value("${url.shortener.base-url}")
    private String baseUrl;

    private final int codeLength = 8;

    public UrlMasterEntity prepareUrlEntity(UrlMasterRequest request, String userCode) {
        UrlMasterEntity urlEntity = new UrlMasterEntity();
        urlEntity.setCode(commonFunctionHelper.generateAlphaNumericCode(codeLength));
        urlEntity.setUrl(encodeUrl(request.getUrl()));
        urlEntity.setShortUrl(baseUrl + urlEntity.getCode());
        urlEntity.setExpiresAt(LocalDateTime.now().plusDays(request.getExpirationDays()));
        urlEntity.setClickCount(0L);
        urlEntity.setStatus(UrlStatusEnums.ACTIVE);
        urlEntity.setLastAccessedAt(null);
        urlEntity.setCreatedBy(userCode);
        urlEntity.setCreatedAt(LocalDateTime.now());
        urlEntity.setUpdatedAt(LocalDateTime.now());
        urlEntity.setDeletedAt(null);
        urlEntity.setDeleted(false);
        return urlEntity;
    }

    public UrlMasterResponse prepareUrlResponse(UrlMasterEntity urlEntity) {
        UrlMasterResponse urlResponse = new UrlMasterResponse();
        urlResponse.setId(urlEntity.getId());
        urlResponse.setCode(urlEntity.getCode());
        urlResponse.setShortUrl(urlEntity.getShortUrl());
        urlResponse.setUrl(decodeUrl(urlEntity.getUrl()));
        urlResponse.setCreatedAt(urlEntity.getCreatedAt());
        urlResponse.setExpiresAt(urlEntity.getExpiresAt());
        urlResponse.setClickCount(urlEntity.getClickCount());
        urlResponse.setStatus(urlEntity.getStatus());
        urlResponse.setLastAccessedAt(urlEntity.getLastAccessedAt());
        urlResponse.setCreatedBy(urlEntity.getCreatedBy());
        return urlResponse;
    }

    public String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (Exception e) {
            throw new ApplicationException("Failed to encode URL: " + e.getMessage());
        }
    }

    public String decodeUrl(String encodedUrl) {
        try {
            return URLDecoder.decode(encodedUrl, "UTF-8");
        } catch (Exception e) {
            throw new ApplicationException("Failed to decode URL: " + e.getMessage());
        }
    }

    /**
     * Generates a random short code of specified length.
     *
     * @return A randomly generated short code string
     */
    public static String generateShortCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < UrlConstants.SHORT_URL_LENGTH; i++) {
            sb.append(UrlConstants.SHORT_URL_CHARACTERS
                    .charAt(RANDOM.nextInt(UrlConstants.SHORT_URL_CHARACTERS.length())));
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
        } catch (ApplicationException e) {
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
        } catch (ApplicationException e) {
            return false;
        }
    }

    /**
     * Validates if a URL matches the required pattern and is properly formatted.
     *
     * @param url The URL to validate
     * @throws ApplicationException if the URL is invalid
     */
    public static void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new ApplicationException("URL cannot be empty");
        }

        // Check URL format
        if (!url.matches(UrlConstants.URL_PATTERN)) {
            throw new ApplicationException("Invalid URL format");
        }

        try {
            // Validate URL structure
            new URL(url).toURI();
        } catch (Exception e) {
            throw new ApplicationException("Invalid URL structure: " + e.getMessage());
        }

        // Check for malicious patterns
        if (url.toLowerCase().contains("javascript:") || url.toLowerCase().contains("data:")
                || url.toLowerCase().contains("vbscript:")) {
            throw new ApplicationException("Invalid URL scheme");
        }

        // Check URL length
        if (url.length() > 2048) {
            throw new ApplicationException("URL too long");
        }
    }

    /**
     * Validates if the expiration days are within acceptable range.
     *
     * @param days The number of days to validate
     * @throws ApplicationException if the days are invalid
     */
    public static void validateExpirationDays(Integer days) {
        if (days == null) {
            throw new ApplicationException("Expiration days cannot be null");
        }

        if (days < UrlConstants.MIN_EXPIRATION_DAYS || days > UrlConstants.MAX_EXPIRATION_DAYS) {
            throw new ApplicationException(String.format("Expiration days must be between %d and %d",
                    UrlConstants.MIN_EXPIRATION_DAYS, UrlConstants.MAX_EXPIRATION_DAYS));
        }
    }
}