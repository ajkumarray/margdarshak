package com.ajkumarray.margdarshak.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ajkumarray.margdarshak.entity.UrlMasterEntity;
import com.ajkumarray.margdarshak.models.request.UrlMasterRequest;
import com.ajkumarray.margdarshak.models.response.UrlMasterResponse;

/**
 * Service interface for URL shortening operations. Provides methods for
 * creating, retrieving, and managing shortened URLs.
 */
@Service
public interface UrlService {

    UrlMasterResponse createShortUrl(UrlMasterRequest request, String userCode);

}