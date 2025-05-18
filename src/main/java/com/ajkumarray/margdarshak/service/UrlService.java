package com.ajkumarray.margdarshak.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ajkumarray.margdarshak.entity.UrlEntity;

/**
 * Service interface for URL shortening operations.
 * Provides methods for creating, retrieving, and managing shortened URLs.
 */
@Service
public interface UrlService {
    /**
     * Creates a short URL for the given original URL.
     *
     * @param originalUrl The original URL to shorten
     * @param expirationDays Number of days until the URL expires
     * @return The created URL entity
     * @throws InvalidUrlException if the URL is invalid or expiration days are invalid
     */
    UrlEntity createShortUrl(String originalUrl, Integer expirationDays);

    /**
     * Gets the original URL for a given short URL.
     * Increments the click count and updates last accessed timestamp.
     *
     * @param shortUrl The short URL to look up
     * @return Optional containing the original URL if found, active, and not expired
     */
    Optional<String> getOriginalUrl(String shortUrl);

    /**
     * Gets statistics for a given short URL.
     *
     * @param shortUrl The short URL to get statistics for
     * @return Optional containing the URL entity (UrlEntity) if found, active, and not expired. The clickCount is a Long.
     */
    Optional<UrlEntity> getUrlStats(String shortUrl);

    /**
     * Updates the expiration date of a short URL.
     *
     * @param shortUrl The short URL to update
     * @param expirationDays New number of days until expiration
     * @return Optional containing the updated URL entity if found, active, and not expired
     * @throws InvalidUrlException if expiration days are invalid
     */
    Optional<UrlEntity> updateUrl(String shortUrl, Integer expirationDays);

    /**
     * Disables a short URL, preventing further access.
     *
     * @param shortUrl The short URL to disable
     * @return Optional containing the disabled URL entity if found
     */
    Optional<UrlEntity> disableUrl(String shortUrl);

    /**
     * Enables a previously disabled short URL.
     *
     * @param shortUrl The short URL to enable
     * @return Optional containing the enabled URL entity if found
     */
    Optional<UrlEntity> enableUrl(String shortUrl);

    /**
     * Updates the status of a short URL (enable/disable).
     *
     * @param shortUrl The short URL to update
     * @param action The action to perform (enable/disable)
     * @return Optional containing the updated URL entity if found
     * @throws InvalidUrlException if the action is invalid
     */
    Optional<UrlEntity> updateUrlStatus(String shortUrl, String action);
} 