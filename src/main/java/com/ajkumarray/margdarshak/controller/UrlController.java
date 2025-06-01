package com.ajkumarray.margdarshak.controller;

import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.util.MessageTranslator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ajkumarray.margdarshak.models.request.UrlMasterRequest;
import com.ajkumarray.margdarshak.service.UrlService;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.ajkumarray.margdarshak.models.response.ObjectResponse;

/**
 * REST Controller for URL shortening operations. Provides endpoints for
 * creating, managing, and accessing shortened URLs.
 * 
 * @author Ajit Kumar
 * @version 1.0
 */
@RestController
@RequestMapping(value = "${spring.api}")
@Tag(name = "URL Shortener", description = "API for URL shortening operations")
public class UrlController {

        private static final String HEADER_USER_CODE = "userCode";

        @Autowired
        private UrlService urlService;

        @PostMapping("url")
        public ResponseEntity<ObjectResponse> createShortUrl(@RequestHeader HttpHeaders headers,
                        @RequestBody UrlMasterRequest request) {

                String userCode = headers.getFirst(HEADER_USER_CODE);

                HttpStatus headerStatus = HttpStatus.BAD_REQUEST;

                ObjectResponse response = new ObjectResponse();
                response.setMessageCode(ApplicationEnums.URL_CREATION_FAILED.getCode());
                response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_CREATION_FAILED.getCode()));

                Object result = urlService.createShortUrl(request, userCode);

                if (result != null) {
                        response.setMessageCode(ApplicationEnums.URL_CREATION_SUCCESS.getCode());
                        response.setMessage(
                                        MessageTranslator.toLocale(ApplicationEnums.URL_CREATION_SUCCESS.getCode()));
                        response.setList(result);
                        headerStatus = HttpStatus.OK;
                }
                return new ResponseEntity<>(response, headerStatus);
        }

}