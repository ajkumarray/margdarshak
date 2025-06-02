package com.ajkumarray.margdarshak.implementation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ajkumarray.margdarshak.entity.UrlMasterEntity;
import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.enums.UrlStatusEnums;
import com.ajkumarray.margdarshak.exception.ApplicationException;
import com.ajkumarray.margdarshak.models.request.UrlMasterRequest;
import com.ajkumarray.margdarshak.models.response.UrlMasterResponse;
import com.ajkumarray.margdarshak.repository.UrlRepository;
import com.ajkumarray.margdarshak.service.UrlService;
import com.ajkumarray.margdarshak.util.UrlHelper;
import com.ajkumarray.margdarshak.util.CommonFunctionHelper;
import com.ajkumarray.margdarshak.util.MessageTranslator;

/**
 * Implementation of the URL service interface. Handles all URL shortening
 * operations including creation, retrieval, and management.
 */
@Component
public class UrlImplementation implements UrlService {

    @Autowired
    private CommonFunctionHelper commonFunctionHelper;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private UrlHelper urlHelper;

    @Override
    public UrlMasterResponse createShortUrl(UrlMasterRequest request, String userCode) {
        try {
            UrlMasterEntity urlEntity = urlRepository.save(urlHelper.prepareUrlEntity(request, userCode));
            return urlHelper.prepareUrlResponse(urlEntity);
        } catch (Exception e) {
            commonFunctionHelper.commonLoggerHelper(e, "UrlImplementation -> createShortUrl failed");
            throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.URL_CREATION_FAILED.getCode()),
                    ApplicationEnums.URL_CREATION_FAILED.getCode());
        }
    }

    @Override
    public List<UrlMasterResponse> getAllUrls(String userCode) {
        try {
            List<UrlMasterEntity> urlEntities = urlRepository.findAllByCreatedByAndStatusAndDeleted(userCode,
                    UrlStatusEnums.ACTIVE, false);
            return urlEntities.stream().map(urlHelper::prepareUrlResponse).collect(Collectors.toList());
        } catch (Exception e) {
            commonFunctionHelper.commonLoggerHelper(e, "UrlImplementation -> getAllUrls failed");
            throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.URL_FAILED_CODE.getCode()),
                    ApplicationEnums.URL_FAILED_CODE.getCode());
        }
    }

    @Override
    public UrlMasterResponse getUrlDetail(String code) {
        try {
            Optional<UrlMasterEntity> urlEntity = urlRepository.findByCodeAndStatusAndDeleted(code,
                    UrlStatusEnums.ACTIVE, false);
            if (urlEntity.isPresent()) {
                return urlHelper.prepareUrlResponse(urlEntity.get());
            } else {
                throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.INVALID_URL_CODE.getCode()),
                        ApplicationEnums.INVALID_URL_CODE.getCode());
            }
        } catch (Exception e) {
            commonFunctionHelper.commonLoggerHelper(e, "UrlImplementation -> getUrlDetail failed");
            throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.URL_FAILED_CODE.getCode()),
                    ApplicationEnums.URL_FAILED_CODE.getCode());
        }
    }

    @Override
    public UrlMasterResponse updateUrl(String code, UrlMasterRequest request) {
        try {
            Optional<UrlMasterEntity> urlEntity = urlRepository.findByCodeAndStatusAndDeleted(code,
                    UrlStatusEnums.ACTIVE, false);
            if (urlEntity.isPresent()) {
                UrlMasterEntity url = urlHelper.prepareUrlUpdateEntity(urlEntity.get(), request);
                urlRepository.save(url);
                return urlHelper.prepareUrlResponse(url);
            } else {
                throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.INVALID_URL_CODE.getCode()),
                        ApplicationEnums.INVALID_URL_CODE.getCode());
            }
        } catch (Exception e) {
            commonFunctionHelper.commonLoggerHelper(e, "UrlImplementation -> updateUrl failed");
            throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.URL_FAILED_CODE.getCode()),
                    ApplicationEnums.URL_FAILED_CODE.getCode());
        }
    }

    @Override
    public UrlMasterResponse updateUrlStatus(String code, String status) {
        try {
            Optional<UrlMasterEntity> urlEntity = urlRepository.findByCodeAndStatusAndDeleted(code,
                    UrlStatusEnums.ACTIVE, false);
            if (urlEntity.isPresent()) {
                UrlMasterEntity url = urlHelper.prepareUrlUpdateStatusEntity(urlEntity.get(), status);
                urlRepository.save(url);
                return urlHelper.prepareUrlResponse(url);
            } else {
                throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.INVALID_URL_CODE.getCode()),
                        ApplicationEnums.INVALID_URL_CODE.getCode());
            }
        } catch (Exception e) {
            commonFunctionHelper.commonLoggerHelper(e, "UrlImplementation -> updateUrlStatus failed");
            throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.URL_FAILED_CODE.getCode()),
                    ApplicationEnums.URL_FAILED_CODE.getCode());
        }
    }

    @Override
    public UrlMasterResponse updateUrlExpire(String code, int days) {
        try {
            Optional<UrlMasterEntity> urlEntity = urlRepository.findByCodeAndStatusAndDeleted(code,
                    UrlStatusEnums.ACTIVE, false);
            if (urlEntity.isPresent()) {
                UrlMasterEntity url = urlHelper.prepareUrlUpdateExpireEntity(urlEntity.get(), days);
                urlRepository.save(url);
                return urlHelper.prepareUrlResponse(url);
            } else {
                throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.INVALID_URL_CODE.getCode()),
                        ApplicationEnums.INVALID_URL_CODE.getCode());
            }
        } catch (Exception e) {
            commonFunctionHelper.commonLoggerHelper(e, "UrlImplementation -> updateUrlExpire failed");
            throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.URL_FAILED_CODE.getCode()),
                    ApplicationEnums.URL_FAILED_CODE.getCode());
        }
    }

    @Override
    public String getOriginalUrl(String code) {
        Optional<UrlMasterEntity> urlEntity = urlRepository.findByCodeAndStatusAndDeleted(code, UrlStatusEnums.ACTIVE,
                false);
        if (urlEntity.isPresent()) {
            UrlMasterEntity url = urlEntity.get();
            return urlHelper.decodeUrl(url.getUrl());
        }
        return null;
    }

}