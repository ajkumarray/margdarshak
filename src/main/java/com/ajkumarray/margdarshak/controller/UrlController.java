package com.ajkumarray.margdarshak.controller;

import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.util.MessageTranslator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

        @GetMapping("urls")
        public ResponseEntity<ObjectResponse> getOriginalUrl(@RequestHeader HttpHeaders headers) {
                String userCode = headers.getFirst(HEADER_USER_CODE);
                ObjectResponse response = new ObjectResponse();
                HttpStatus headerStatus = HttpStatus.BAD_REQUEST;
                response.setMessageCode(ApplicationEnums.URL_FAILED_CODE.getCode());
                response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_FAILED_CODE.getCode()));

                Object result = urlService.getAllUrls(userCode);

                if (result != null) {
                        response.setMessageCode(ApplicationEnums.URL_SUCCESS_CODE.getCode());
                        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_SUCCESS_CODE.getCode()));
                        response.setList(result);
                        headerStatus = HttpStatus.OK;
                }
                return new ResponseEntity<>(response, headerStatus);
        }

        @GetMapping("url/detail/{code}")
        public ResponseEntity<ObjectResponse> getUrlDetail(@PathVariable String code) {
                ObjectResponse response = new ObjectResponse();
                HttpStatus headerStatus = HttpStatus.BAD_REQUEST;
                response.setMessageCode(ApplicationEnums.URL_FAILED_CODE.getCode());
                response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_FAILED_CODE.getCode()));

                Object result = urlService.getUrlDetail(code);

                if (result != null) {
                        response.setMessageCode(ApplicationEnums.URL_SUCCESS_CODE.getCode());
                        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_SUCCESS_CODE.getCode()));
                        response.setList(result);
                        headerStatus = HttpStatus.OK;
                }
                return new ResponseEntity<>(response, headerStatus);
        }

        @PutMapping("url/update/{code}")
        public ResponseEntity<ObjectResponse> updateUrl(@PathVariable String code,
                        @RequestBody UrlMasterRequest request) {
                ObjectResponse response = new ObjectResponse();
                HttpStatus headerStatus = HttpStatus.BAD_REQUEST;
                response.setMessageCode(ApplicationEnums.URL_FAILED_CODE.getCode());
                response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_FAILED_CODE.getCode()));

                Object result = urlService.updateUrl(code, request);

                if (result != null) {
                        response.setMessageCode(ApplicationEnums.URL_SUCCESS_CODE.getCode());
                        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_SUCCESS_CODE.getCode()));
                }
                return new ResponseEntity<>(response, headerStatus);
        }

        @PutMapping("url/status/{code}/{status}")
        public ResponseEntity<ObjectResponse> updateUrlStatus(@PathVariable String code, @PathVariable String status) {
                ObjectResponse response = new ObjectResponse();
                HttpStatus headerStatus = HttpStatus.BAD_REQUEST;
                response.setMessageCode(ApplicationEnums.URL_FAILED_CODE.getCode());
                response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_FAILED_CODE.getCode()));

                Object result = urlService.updateUrlStatus(code, status);

                if (result != null) {
                        response.setMessageCode(ApplicationEnums.URL_SUCCESS_CODE.getCode());
                        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_SUCCESS_CODE.getCode()));
                }
                return new ResponseEntity<>(response, headerStatus);
        }

        @PutMapping("url/expire/{code}/{days}")
        public ResponseEntity<ObjectResponse> updateUrlExpire(@PathVariable String code, @PathVariable int days) {
                ObjectResponse response = new ObjectResponse();
                HttpStatus headerStatus = HttpStatus.BAD_REQUEST;
                response.setMessageCode(ApplicationEnums.URL_FAILED_CODE.getCode());
                response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_FAILED_CODE.getCode()));

                Object result = urlService.updateUrlExpire(code, days);

                if (result != null) {
                        response.setMessageCode(ApplicationEnums.URL_SUCCESS_CODE.getCode());
                        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_SUCCESS_CODE.getCode()));
                }
                return new ResponseEntity<>(response, headerStatus);
        }
}