package com.ajkumarray.margdarshak.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ajkumarray.margdarshak.constants.UrlConstants;
import com.ajkumarray.margdarshak.entity.Url;
import com.ajkumarray.margdarshak.entity.UrlStatus;
import com.ajkumarray.margdarshak.exception.InvalidUrlException;
import com.ajkumarray.margdarshak.repository.UrlRepository;
import com.ajkumarray.margdarshak.service.UrlService;
import com.ajkumarray.margdarshak.util.UrlGenerator;

/**
 * Implementation of the URL service interface.
 * Handles all URL shortening operations including creation, retrieval, and management.
 */
@Service
public class UrlServiceImpl implements UrlService {
    private final UrlRepository urlRepository;
    private final UrlGenerator urlGenerator;

    @Autowired
    public UrlServiceImpl(UrlRepository urlRepository, UrlGenerator urlGenerator) {
        this.urlRepository = urlRepository;
        this.urlGenerator = urlGenerator;
    }

    /**
     * Creates a new short URL for the given original URL.
     * Validates the input, generates a unique short URL, and persists the URL entity.
     *
     * @param originalUrl The original URL to be shortened
     * @param expirationDays Number of days until the URL expires
     * @return The created URL entity
     * @throws InvalidUrlException if the URL is invalid or expiration days are invalid
     */
    @Override
    @Transactional
    public Url createShortUrl(String originalUrl, Integer expirationDays) {
        validateInput(originalUrl, expirationDays);
        String shortUrl = generateUniqueShortUrl();
        LocalDateTime now = LocalDateTime.now();

        Url url = Url.builder()
            .url(originalUrl)
            .shortUrl(shortUrl)
            .createdAt(now)
            .expiresAt(now.plusDays(expirationDays))
            .clickCount(0)
            .status(UrlStatus.ACTIVE)
            .lastAccessedAt(null)
            .createdBy(UrlConstants.SYSTEM_USER)
            .build();

        return urlRepository.save(url);
    }

    /**
     * Retrieves the original URL for a given short URL.
     * Increments the click count and updates the last accessed timestamp.
     *
     * @param shortUrl The short URL to look up
     * @return Optional containing the original URL if found and active
     */
    @Override
    @Transactional
    public Optional<String> getOriginalUrl(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl)
                .filter(url -> url.getStatus() == UrlStatus.ACTIVE)
                .filter(url -> url.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(url -> {
                    url.setClickCount(url.getClickCount() + 1);
                    url.setLastAccessedAt(LocalDateTime.now());
                    urlRepository.save(url);
                    return url.getUrl();
                });
    }

    /**
     * Retrieves statistics for a given short URL.
     *
     * @param shortUrl The short URL to get statistics for
     * @return Optional containing the URL entity if found and active
     */
    @Override
    public Optional<Url> getUrlStats(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl)
                .filter(url -> url.getStatus() == UrlStatus.ACTIVE)
                .filter(url -> url.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    /**
     * Updates the expiration date of a short URL.
     *
     * @param shortUrl The short URL to update
     * @param expirationDays New number of days until expiration
     * @return Optional containing the updated URL entity if found and active
     * @throws InvalidUrlException if expiration days are invalid
     */
    @Override
    @Transactional
    public Optional<Url> updateUrl(String shortUrl, Integer expirationDays) {
        if (!UrlGenerator.isValidExpirationDays(expirationDays)) {
            throw new InvalidUrlException("Expiration days must be between " + 
                UrlConstants.MIN_EXPIRATION_DAYS + " and " + 
                UrlConstants.MAX_EXPIRATION_DAYS);
        }
        
        return urlRepository.findByShortUrl(shortUrl)
                .filter(url -> url.getStatus() == UrlStatus.ACTIVE)
                .filter(url -> url.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(url -> {
                    url.setExpiresAt(LocalDateTime.now().plusDays(expirationDays));
                    return urlRepository.save(url);
                });
    }

    /**
     * Disables a short URL, preventing further access.
     *
     * @param shortUrl The short URL to disable
     * @return Optional containing the disabled URL entity if found
     */
    @Override
    @Transactional
    public Optional<Url> disableUrl(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl)
                .map(url -> {
                    url.setStatus(UrlStatus.DISABLED);
                    return urlRepository.save(url);
                });
    }

    /**
     * Enables a previously disabled short URL.
     *
     * @param shortUrl The short URL to enable
     * @return Optional containing the enabled URL entity if found
     */
    @Override
    @Transactional
    public Optional<Url> enableUrl(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl)
                .map(url -> {
                    url.setStatus(UrlStatus.ACTIVE);
                    return urlRepository.save(url);
                });
    }

    /**
     * Generates a unique short URL that doesn't exist in the database.
     *
     * @return A unique short URL string
     * @throws RuntimeException if unable to generate a unique URL after maximum retries
     */
    private String generateUniqueShortUrl() {
        String shortCode;
        int retries = 0;
        
        do {
            if (retries >= UrlConstants.MAX_SHORT_URL_GENERATION_RETRIES) {
                throw new RuntimeException("Failed to generate unique short URL after " + 
                    UrlConstants.MAX_SHORT_URL_GENERATION_RETRIES + " attempts");
            }
            shortCode = UrlGenerator.generateShortCode();
            retries++;
        } while (urlRepository.existsByShortUrl(shortCode));

        return shortCode;
    }

    /**
     * Validates the input parameters for URL creation.
     *
     * @param url The URL to validate
     * @param expirationDays The expiration days to validate
     * @throws InvalidUrlException if either parameter is invalid
     */
    private void validateInput(String url, Integer expirationDays) {
        if (!UrlGenerator.isValidUrl(url)) {
            throw new InvalidUrlException("Invalid URL format");
        }
        if (!UrlGenerator.isValidExpirationDays(expirationDays)) {
            throw new InvalidUrlException("Expiration days must be between " + 
                UrlConstants.MIN_EXPIRATION_DAYS + " and " + 
                UrlConstants.MAX_EXPIRATION_DAYS);
        }
    }

    @Override
    public Optional<Url> updateUrlStatus(String shortUrl, String action) {
        if (!action.equalsIgnoreCase("enable") && !action.equalsIgnoreCase("disable")) {
            throw new InvalidUrlException("Invalid action. Must be either 'enable' or 'disable'");
        }
        boolean enable = action.equalsIgnoreCase("enable");
        return enable ? enableUrl(shortUrl) : disableUrl(shortUrl);
    }
} 