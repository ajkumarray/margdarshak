package com.ajkumarray.margdarshak.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.ajkumarray.margdarshak.service.UrlService;
import com.ajkumarray.margdarshak.util.MessageTranslator;
import com.ajkumarray.margdarshak.util.CommonFunctionHelper;
import com.ajkumarray.margdarshak.models.response.ObjectResponse;
import com.ajkumarray.margdarshak.models.request.UrlMasterRequest;
import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.exception.ApplicationException;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.HttpHeaders;

import lombok.AllArgsConstructor;

/**
 * Controller for handling public URL shortening operations. Provides endpoints
 * for creating shortened URLs for external users.
 * 
 * All endpoints require a mandatory 'userCode' in the request header for user
 * identification.
 */
@RestController
@RequestMapping(value = "${spring.api}public/")
@Tag(name = "Public URL Shortener", description = "Public API for URL shortening operations. Requires userCode in header for all requests.")
@AllArgsConstructor
public class PublicController {

        private final UrlService urlService;
        private final CommonFunctionHelper commonFunctionHelper;

        /**
         * Creates a new shortened URL for external users.
         *
         * @param request The URL shortening request containing the original URL
         * @param headers HTTP headers containing the mandatory userCode for user
         *                identification
         * @return ResponseEntity containing the shortened URL information
         * @throws IllegalArgumentException if userCode is missing in the request header
         */
        @Operation(summary = "Create Short URL", description = "Creates a new shortened URL for the provided long URL. Requires userCode in header for user identification.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "URL successfully shortened", content = @Content(schema = @Schema(implementation = ObjectResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request parameters or missing userCode in header") })
        @PostMapping("url")
        public ResponseEntity<ObjectResponse> createShortUrl(
                        @Parameter(description = "URL shortening request") @RequestBody UrlMasterRequest request,
                        @Parameter(description = "HTTP headers containing the mandatory userCode", required = true) @RequestHeader HttpHeaders headers)
                        throws ApplicationException {
                String userCode = headers.getFirst("userCode");
                if (commonFunctionHelper.isEmptyOrBlank(userCode)) {
                        throw new ApplicationException(
                                        MessageTranslator.toLocale(ApplicationEnums.INVALID_HEADER_REQUEST.getCode()),
                                        ApplicationEnums.INVALID_HEADER_REQUEST.getCode());
                }

                ObjectResponse response = new ObjectResponse();
                HttpStatus headerStatus = HttpStatus.OK;
                response.setMessageCode(ApplicationEnums.URL_CREATION_SUCCESS.getCode());
                response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_CREATION_SUCCESS.getCode()));

                Object result = urlService.createShortUrl(request, userCode);
                response.setList(result);

                return new ResponseEntity<>(response, headerStatus);
        }

}
