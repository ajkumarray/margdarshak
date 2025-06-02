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
        urlEntity.setStatus(request.getStatus());
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

    public UrlMasterEntity prepareUrlUpdateEntity(UrlMasterEntity urlEntity, UrlMasterRequest request) {
        urlEntity.setUrl(encodeUrl(request.getUrl()));
        urlEntity.setExpiresAt(LocalDateTime.now().plusDays(request.getExpirationDays()));
        urlEntity.setStatus(request.getStatus());
        urlEntity.setUpdatedAt(LocalDateTime.now());
        return urlEntity;
    }

    public UrlMasterEntity prepareUrlUpdateStatusEntity(UrlMasterEntity urlEntity, String status) {
        urlEntity.setStatus(UrlStatusEnums.valueOf(status));
        urlEntity.setUpdatedAt(LocalDateTime.now());
        return urlEntity;
    }

    public UrlMasterEntity prepareUrlUpdateExpireEntity(UrlMasterEntity urlEntity, int days) {
        urlEntity.setExpiresAt(LocalDateTime.now().plusDays(days));
        urlEntity.setUpdatedAt(LocalDateTime.now());
        return urlEntity;
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

}