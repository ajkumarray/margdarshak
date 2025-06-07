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

import lombok.AllArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.ajkumarray.margdarshak.models.response.ObjectResponse;

/**
 * Controller for handling URL shortening operations. Provides endpoints for
 * creating, retrieving, and managing shortened URLs.
 */
@RestController
@RequestMapping(value = "${spring.api}url")
@Tag(name = "URL Shortener", description = "API for URL shortening operations")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
public class UrlController {

        private final UrlService urlService;

        /**
         * Creates a new shortened URL.
         *
         * @param headers HTTP headers containing user information
         * @param request The URL shortening request containing the original URL and
         *                optional expiration
         * @return ResponseEntity containing the shortened URL information
         */
        @Operation(summary = "Create Short URL", description = "Creates a new shortened URL for the provided long URL")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "URL successfully shortened", content = @Content(schema = @Schema(implementation = ObjectResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required") })
        @PostMapping("")
        public ResponseEntity<ObjectResponse> createShortUrl(
                        @Parameter(description = "HTTP Headers") @RequestHeader HttpHeaders headers,
                        @Parameter(description = "URL shortening request") @RequestBody UrlMasterRequest request) {

                String userCode = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                HttpStatus headerStatus = HttpStatus.OK;
                ObjectResponse response = new ObjectResponse();
                response.setMessageCode(ApplicationEnums.URL_CREATION_SUCCESS.getCode());
                response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_CREATION_SUCCESS.getCode()));

                Object result = urlService.createShortUrl(request, userCode);
                response.setList(result);

                return new ResponseEntity<>(response, headerStatus);
        }

        /**
         * Retrieves all URLs for the authenticated user.
         *
         * @param headers HTTP headers containing user information
         * @return ResponseEntity containing the list of user's URLs
         */
        @Operation(summary = "Get All URLs", description = "Retrieves all shortened URLs for the authenticated user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved URLs", content = @Content(schema = @Schema(implementation = ObjectResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required") })
        @GetMapping("")
        public ResponseEntity<ObjectResponse> getOriginalUrl(
                        @Parameter(description = "HTTP Headers") @RequestHeader HttpHeaders headers) {
                String userCode = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                ObjectResponse response = new ObjectResponse();
                HttpStatus headerStatus = HttpStatus.OK;
                response.setMessageCode(ApplicationEnums.URL_SUCCESS_CODE.getCode());
                response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_SUCCESS_CODE.getCode()));

                Object result = urlService.getAllUrls(userCode);
                response.setList(result);

                return new ResponseEntity<>(response, headerStatus);
        }

        /**
         * Retrieves detailed information about a specific shortened URL.
         *
         * @param code The short URL code
         * @return ResponseEntity containing the URL details
         */
        @Operation(summary = "Get URL Details", description = "Retrieves detailed information about a specific shortened URL")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved URL details", content = @Content(schema = @Schema(implementation = ObjectResponse.class))),
                        @ApiResponse(responseCode = "404", description = "URL not found") })
        @GetMapping("/detail/{code}")
        public ResponseEntity<ObjectResponse> getUrlDetail(
                        @Parameter(description = "Short URL code") @PathVariable String code) {
                ObjectResponse response = new ObjectResponse();
                HttpStatus headerStatus = HttpStatus.OK;
                response.setMessageCode(ApplicationEnums.URL_SUCCESS_CODE.getCode());
                response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_SUCCESS_CODE.getCode()));

                Object result = urlService.getUrlDetail(code);
                response.setList(result);

                return new ResponseEntity<>(response, headerStatus);
        }

        /**
         * Updates an existing shortened URL.
         *
         * @param code    The short URL code to update
         * @param request The updated URL information
         * @return ResponseEntity containing the updated URL information
         */
        @Operation(summary = "Update URL", description = "Updates an existing shortened URL with new information")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully updated URL", content = @Content(schema = @Schema(implementation = ObjectResponse.class))),
                        @ApiResponse(responseCode = "404", description = "URL not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required") })
        @PutMapping("/update/{code}")
        public ResponseEntity<ObjectResponse> updateUrl(
                        @Parameter(description = "Short URL code") @PathVariable String code,
                        @Parameter(description = "Updated URL information") @RequestBody UrlMasterRequest request) {
                ObjectResponse response = new ObjectResponse();
                HttpStatus headerStatus = HttpStatus.OK;

                Object result = urlService.updateUrl(code, request);
                response.setList(result);
                return new ResponseEntity<>(response, headerStatus);
        }
}