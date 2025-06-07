package com.ajkumarray.margdarshak.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import java.net.URI;

import com.ajkumarray.margdarshak.service.UrlService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for handling URL redirections. Provides endpoints for redirecting
 * shortened URLs to their original destinations.
 */
@RestController
@RequestMapping(value = "")
@Tag(name = "URL Redirection", description = "API for redirecting shortened URLs to their original destinations")
@AllArgsConstructor
public class RedirectController {

    private final UrlService urlService;

    /**
     * Redirects a shortened URL to its original destination.
     *
     * @param code The short URL code to redirect
     * @return ResponseEntity with HTTP 302 (Found) redirect to the original URL, or
     *         404 if not found
     */
    @Operation(summary = "Redirect to Original URL", description = "Redirects a shortened URL to its original destination")
    @ApiResponses(value = { @ApiResponse(responseCode = "302", description = "Successfully redirected to original URL"),
            @ApiResponse(responseCode = "404", description = "Short URL not found or expired") })
    @GetMapping(value = "{code}")
    public ResponseEntity<Void> redirectToOriginalUrl(
            @Parameter(description = "Short URL code") @PathVariable String code) {
        String originalUrl = urlService.getOriginalUrl(code);
        if (originalUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(originalUrl)).build();
    }
}
