package com.ajkumarray.margdarshak.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ajkumarray.margdarshak.models.request.UrlMasterRequest;
import com.ajkumarray.margdarshak.models.response.UrlMasterResponse;

@Service
public interface UrlService {

    UrlMasterResponse createShortUrl(UrlMasterRequest request, String userCode);

    List<UrlMasterResponse> getAllUrls(String userCode);

    UrlMasterResponse getUrlDetail(String code);

    UrlMasterResponse updateUrl(String code, UrlMasterRequest request);

    String getOriginalUrl(String code);

}