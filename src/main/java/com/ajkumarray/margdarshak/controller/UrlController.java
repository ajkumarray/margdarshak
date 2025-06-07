package com.ajkumarray.margdarshak.controller;

import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.util.MessageTranslator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ajkumarray.margdarshak.models.request.UrlMasterRequest;
import com.ajkumarray.margdarshak.service.UrlService;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.ajkumarray.margdarshak.models.response.ObjectResponse;

@RestController
@RequestMapping(value = "${spring.api}url")
@Tag(name = "URL Shortener", description = "API for URL shortening operations")
public class UrlController {

        private static final String HEADER_USER_CODE = "userCode";

        @Autowired
        private UrlService urlService;

        @PostMapping("")
        public ResponseEntity<ObjectResponse> createShortUrl(@RequestHeader HttpHeaders headers,
                        @RequestBody UrlMasterRequest request) {

                String userCode = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                HttpStatus headerStatus = HttpStatus.OK;
                ObjectResponse response = new ObjectResponse();
                response.setMessageCode(ApplicationEnums.URL_CREATION_SUCCESS.getCode());
                response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_CREATION_SUCCESS.getCode()));

                Object result = urlService.createShortUrl(request, userCode);
                response.setList(result);

                return new ResponseEntity<>(response, headerStatus);
        }

        @GetMapping("")
        public ResponseEntity<ObjectResponse> getOriginalUrl(@RequestHeader HttpHeaders headers) {
                String userCode = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                ObjectResponse response = new ObjectResponse();
                HttpStatus headerStatus = HttpStatus.OK;
                response.setMessageCode(ApplicationEnums.URL_SUCCESS_CODE.getCode());
                response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_SUCCESS_CODE.getCode()));

                Object result = urlService.getAllUrls(userCode);
                response.setList(result);

                return new ResponseEntity<>(response, headerStatus);
        }

        @GetMapping("/detail/{code}")
        public ResponseEntity<ObjectResponse> getUrlDetail(@PathVariable String code) {
                ObjectResponse response = new ObjectResponse();
                HttpStatus headerStatus = HttpStatus.OK;
                response.setMessageCode(ApplicationEnums.URL_SUCCESS_CODE.getCode());
                response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_SUCCESS_CODE.getCode()));

                Object result = urlService.getUrlDetail(code);
                response.setList(result);

                return new ResponseEntity<>(response, headerStatus);
        }

        @PutMapping("/update/{code}")
        public ResponseEntity<ObjectResponse> updateUrl(@PathVariable String code,
                        @RequestBody UrlMasterRequest request) {
                ObjectResponse response = new ObjectResponse();
                HttpStatus headerStatus = HttpStatus.OK;

                Object result = urlService.updateUrl(code, request);
                response.setList(result);
                return new ResponseEntity<>(response, headerStatus);
        }

}