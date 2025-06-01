package com.ajkumarray.margdarshak.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ajkumarray.margdarshak.entity.UrlMasterEntity;
import com.ajkumarray.margdarshak.enums.ApplicationEnums;
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

    // @Override
    // @Transactional
    // public Optional<String> getOriginalUrl(String shortUrl) {
    // return urlRepository.findByShortUrl(shortUrl).filter(url -> url.getStatus()
    // == UrlStatusEnums.ACTIVE)
    // .filter(url -> url.getExpiresAt().isAfter(LocalDateTime.now())).map(url -> {
    // url.setClickCount(url.getClickCount() + 1L);
    // url.setLastAccessedAt(LocalDateTime.now());
    // urlRepository.save(url);
    // return url.getUrl();
    // });
    // }

    // @Override
    // public Optional<UrlMasterEntity> getUrlStats(String shortUrl) {
    // return urlRepository.findByShortUrl(shortUrl).filter(url -> url.getStatus()
    // == UrlStatusEnums.ACTIVE)
    // .filter(url -> url.getExpiresAt().isAfter(LocalDateTime.now()));
    // }

    // @Override
    // @Transactional
    // public Optional<UrlMasterEntity> updateUrl(String shortUrl, Integer
    // expirationDays) {
    // if (!UrlHelper.isValidExpirationDays(expirationDays)) {
    // throw new ApplicationException("Expiration days must be between " +
    // UrlConstants.MIN_EXPIRATION_DAYS
    // + " and " + UrlConstants.MAX_EXPIRATION_DAYS);
    // }

    // return urlRepository.findByShortUrl(shortUrl).filter(url -> url.getStatus()
    // == UrlStatusEnums.ACTIVE)
    // .filter(url -> url.getExpiresAt().isAfter(LocalDateTime.now())).map(url -> {
    // url.setExpiresAt(LocalDateTime.now().plusDays(expirationDays));
    // return urlRepository.save(url);
    // });
    // }

    // @Override
    // @Transactional
    // public Optional<UrlMasterEntity> disableUrl(String shortUrl) {
    // return urlRepository.findByShortUrl(shortUrl).map(url -> {
    // url.setStatus(UrlStatusEnums.DISABLED);
    // return urlRepository.save(url);
    // });
    // }

    // @Override
    // @Transactional
    // public Optional<UrlMasterEntity> enableUrl(String shortUrl) {
    // return urlRepository.findByShortUrl(shortUrl).map(url -> {
    // url.setStatus(UrlStatusEnums.ACTIVE);
    // return urlRepository.save(url);
    // });
    // }

    // private String generateUniqueShortUrl() {
    // String shortCode;
    // int retries = 0;

    // do {
    // if (retries >= UrlConstants.MAX_SHORT_URL_GENERATION_RETRIES) {
    // throw new RuntimeException("Failed to generate unique short URL after "
    // + UrlConstants.MAX_SHORT_URL_GENERATION_RETRIES + " attempts");
    // }
    // shortCode = UrlHelper.generateShortCode();
    // retries++;
    // } while (urlRepository.existsByShortUrl(shortCode));

    // return shortCode;
    // }

    // private void validateInput(String url, Integer expirationDays) {
    // if (!UrlHelper.isValidUrl(url)) {
    // throw new ApplicationException("Invalid URL format");
    // }
    // if (!UrlHelper.isValidExpirationDays(expirationDays)) {
    // throw new ApplicationException("Expiration days must be between " +
    // UrlConstants.MIN_EXPIRATION_DAYS
    // + " and " + UrlConstants.MAX_EXPIRATION_DAYS);
    // }
    // }

    // @Override
    // public Optional<UrlMasterEntity> updateUrlStatus(String shortUrl, String
    // action) {
    // if (!action.equalsIgnoreCase("enable") &&
    // !action.equalsIgnoreCase("disable")) {
    // throw new ApplicationException("Invalid action. Must be either 'enable' or
    // 'disable'");
    // }
    // boolean enable = action.equalsIgnoreCase("enable");
    // return enable ? enableUrl(shortUrl) : disableUrl(shortUrl);
    // }
}