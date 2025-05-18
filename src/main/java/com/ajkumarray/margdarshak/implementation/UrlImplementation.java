package com.ajkumarray.margdarshak.implementation;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ajkumarray.margdarshak.constants.UrlConstants;
import com.ajkumarray.margdarshak.entity.UrlEntity;
import com.ajkumarray.margdarshak.enums.UrlStatusEnums;
import com.ajkumarray.margdarshak.exception.InvalidUrlException;
import com.ajkumarray.margdarshak.repository.UrlRepository;
import com.ajkumarray.margdarshak.service.UrlService;
import com.ajkumarray.margdarshak.util.UrlGenerator;

/**
 * Implementation of the URL service interface.
 * Handles all URL shortening operations including creation, retrieval, and management.
 */
@Component
public class UrlImplementation implements UrlService {
    @Autowired
    private UrlRepository urlRepository;
    
    @Autowired
    private UrlGenerator urlGenerator;

    @Override
    @Transactional
    public UrlEntity createShortUrl(String originalUrl, Integer expirationDays) {
        validateInput(originalUrl, expirationDays);
        String shortUrl = generateUniqueShortUrl();
        LocalDateTime now = LocalDateTime.now();

        UrlEntity url = UrlEntity.builder()
            .url(originalUrl)
            .shortUrl(shortUrl)
            .createdAt(now)
            .expiresAt(now.plusDays(expirationDays))
            .clickCount(0L)
            .status(UrlStatusEnums.ACTIVE)
            .lastAccessedAt(null)
            .createdBy(UrlConstants.SYSTEM_USER)
            .build();

        return urlRepository.save(url);
    }

    @Override
    @Transactional
    public Optional<String> getOriginalUrl(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl)
                .filter(url -> url.getStatus() == UrlStatusEnums.ACTIVE)
                .filter(url -> url.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(url -> {
                    url.setClickCount(url.getClickCount() + 1L);
                    url.setLastAccessedAt(LocalDateTime.now());
                    urlRepository.save(url);
                    return url.getUrl();
                });
    }

    @Override
    public Optional<UrlEntity> getUrlStats(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl)
                .filter(url -> url.getStatus() == UrlStatusEnums.ACTIVE)
                .filter(url -> url.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Override
    @Transactional
    public Optional<UrlEntity> updateUrl(String shortUrl, Integer expirationDays) {
        if (!UrlGenerator.isValidExpirationDays(expirationDays)) {
            throw new InvalidUrlException("Expiration days must be between " + 
                UrlConstants.MIN_EXPIRATION_DAYS + " and " + 
                UrlConstants.MAX_EXPIRATION_DAYS);
        }
        
        return urlRepository.findByShortUrl(shortUrl)
                .filter(url -> url.getStatus() == UrlStatusEnums.ACTIVE)
                .filter(url -> url.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(url -> {
                    url.setExpiresAt(LocalDateTime.now().plusDays(expirationDays));
                    return urlRepository.save(url);
                });
    }

    @Override
    @Transactional
    public Optional<UrlEntity> disableUrl(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl)
                .map(url -> {
                    url.setStatus(UrlStatusEnums.DISABLED);
                    return urlRepository.save(url);
                });
    }

    @Override
    @Transactional
    public Optional<UrlEntity> enableUrl(String shortUrl) {
        return urlRepository.findByShortUrl(shortUrl)
                .map(url -> {
                    url.setStatus(UrlStatusEnums.ACTIVE);
                    return urlRepository.save(url);
                });
    }

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
    public Optional<UrlEntity> updateUrlStatus(String shortUrl, String action) {
        if (!action.equalsIgnoreCase("enable") && !action.equalsIgnoreCase("disable")) {
            throw new InvalidUrlException("Invalid action. Must be either 'enable' or 'disable'");
        }
        boolean enable = action.equalsIgnoreCase("enable");
        return enable ? enableUrl(shortUrl) : disableUrl(shortUrl);
    }
} 