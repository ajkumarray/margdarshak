package com.ajkumarray.margdarshak.controller;

import java.util.Map;
import java.util.Optional;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ajkumarray.margdarshak.models.request.UrlCreateRequest;
import com.ajkumarray.margdarshak.models.response.UrlShortenResponse;
import com.ajkumarray.margdarshak.models.response.UrlStatsResponse;
import com.ajkumarray.margdarshak.entity.UrlEntity;
import com.ajkumarray.margdarshak.exception.InvalidUrlException;
import com.ajkumarray.margdarshak.service.UrlService;
import com.ajkumarray.margdarshak.validator.UrlValidator;
import com.ajkumarray.margdarshak.util.UrlGenerator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Value;

/**
 * REST Controller for URL shortening operations. Provides endpoints for
 * creating, managing, and accessing shortened URLs.
 * 
 * @author Ajit Kumar
 * @version 1.0
 */
@RestController
@RequestMapping(value = "/api/v1/urls", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "URL Shortener", description = "API for URL shortening operations")
public class UrlController {
    @Autowired
    private UrlService urlService;

    @Autowired
    private UrlValidator urlValidator;

    @Value("${url.shortener.base-url:http://localhost:8080/api/v1/urls/}")
    private String baseUrl;

    /**
     * Creates a short URL for the given URL.
     *
     * @param request The URL creation request containing the URL and expiration days
     * @return ResponseEntity with the created short URL and expiration date (clickCount is a Long)
     * @throws InvalidUrlException if the URL is invalid or expiration days are invalid
     */
    @Operation(summary = "Create a short URL", description = "Creates a new short URL for the given URL with specified expiration days.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UrlCreateRequest.class), examples = {
            @ExampleObject(name = "Valid Request", value = "{\"url\": \"https://example.com\", \"expirationDays\": 30}") })))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL shortened successfully", content = @Content(schema = @Schema(implementation = UrlShortenResponse.class), examples = {
                    @ExampleObject(name = "Success Response", value = "{\"code\": \"abc123\", \"shortUrl\": \"http://localhost:8080/api/v1/urls/abc123\", \"url\": \"https://example.com\", \"createdAt\": \"2024-05-10T12:00:00\", \"expiresAt\": \"2024-06-10T12:00:00\", \"clickCount\": 0}") })),
            @ApiResponse(responseCode = "400", description = "Invalid URL or expiration days", content = @Content(schema = @Schema(implementation = UrlShortenResponse.class), examples = {
                    @ExampleObject(name = "Error Response", value = "{\"errorMessage\": \"Invalid URL format\"}") })),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = UrlShortenResponse.class), examples = {
                    @ExampleObject(name = "Error Response", value = "{\"errorMessage\": \"Failed to generate unique short URL. Please try again.\"}") })) })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UrlShortenResponse> createShortUrl(@Valid @RequestBody UrlCreateRequest request) {
        try {
            UrlGenerator.validateUrl(request.getUrl());
            UrlGenerator.validateExpirationDays(request.getExpirationDays());
            UrlEntity url = urlService.createShortUrl(request.getUrl(), request.getExpirationDays());
            return ResponseEntity.ok(UrlShortenResponse.builder()
                    .shortUrl(url.getShortUrl())
                    .originalUrl(url.getUrl())
                    .expiresAt(url.getExpiresAt())
                    .createdAt(url.getCreatedAt())
                    .clickCount(url.getClickCount())
                    .status(url.getStatus())
                    .build());
        } catch (InvalidUrlException e) {
            return ResponseEntity.badRequest().body(new UrlShortenResponse(e.getMessage()));
        } catch (RuntimeException e) {
            String errorMessage = "Failed to generate unique short URL. Please try again.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UrlShortenResponse(errorMessage));
        }
    }

    /**
     * Redirects to the original URL.
     *
     * @param shortUrl The short URL to redirect from
     * @return ResponseEntity with the original URL if found and active
     */
    @Operation(summary = "Redirect to original URL", description = "Retrieves and redirects to the original URL for a given short URL", parameters = {
            @Parameter(name = "shortUrl", description = "The short URL to redirect from", example = "abc123", required = true) })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Original URL retrieved successfully", content = @Content(schema = @Schema(type = "string"), examples = {
                    @ExampleObject(name = "Success Response", value = "https://example.com") })),
            @ApiResponse(responseCode = "404", description = "URL not found, expired, or disabled", content = @Content(schema = @Schema(type = "string"), examples = {
                    @ExampleObject(name = "Error Response", value = "URL not found") })) })
    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirectToOriginalUrl(
            @Parameter(description = "The short URL to redirect from", required = true) @PathVariable String shortUrl) {
        return urlService.getOriginalUrl(shortUrl)
                .map(url -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(url))
                        .<Void>build())
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Gets information about a short URL.
     *
     * @param shortUrl The short URL to get information for
     * @return ResponseEntity with URL information if found and active
     */
    @Operation(summary = "Get URL information", description = "Retrieves basic information about a short URL")
    @ApiResponses({ 
            @ApiResponse(responseCode = "200", description = "URL information retrieved successfully", content = @Content(schema = @Schema(type = "object"), examples = {
                    @ExampleObject(name = "Success Response", value = "{\"url\": \"https://example.com\", \"shortUrl\": \"http://localhost:8080/api/v1/urls/abc123\"}") })),
            @ApiResponse(responseCode = "404", description = "URL not found, expired, or disabled") })
    @GetMapping("/{shortUrl}/info")
    public ResponseEntity<?> getUrlInfo(
            @Parameter(description = "The short URL to get information for", required = true) @PathVariable String shortUrl) {
        return urlService.getOriginalUrl(shortUrl)
                .map(originalUrl -> ResponseEntity.ok().body(Map.of(
                    "url", originalUrl,
                    "shortUrl", baseUrl + shortUrl
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Gets statistics for a short URL.
     *
     * @param shortUrl The short URL to get statistics for
     * @return ResponseEntity with URL statistics if found and active (clickCount is a Long)
     */
    @Operation(summary = "Get URL statistics", description = "Retrieves detailed statistics for a short URL including click count (Long) and timestamps", parameters = {
            @Parameter(name = "shortUrl", description = "The short URL to get statistics for", example = "abc123", required = true) })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully", content = @Content(schema = @Schema(implementation = UrlStatsResponse.class), examples = {
                    @ExampleObject(name = "Success Response", value = "{\"url\": \"https://example.com\", \"shortUrl\": \"http://localhost:8080/api/v1/urls/abc123\", \"clickCount\": 10, \"createdAt\": \"2024-04-10T12:00:00\", \"expiresAt\": \"2024-05-10T12:00:00\"}") })),
            @ApiResponse(responseCode = "404", description = "URL not found, expired, or disabled", content = @Content(schema = @Schema(type = "string"), examples = {
                    @ExampleObject(name = "Error Response", value = "URL not found") })) })
    @GetMapping("/{shortUrl}/stats")
    public ResponseEntity<UrlStatsResponse> getUrlStats(
            @Parameter(description = "The short URL to get statistics for", required = true) @PathVariable String shortUrl) {
        Optional<UrlEntity> urlOptional = urlService.getUrlStats(shortUrl);
        if (urlOptional.isPresent()) {
            UrlEntity url = urlOptional.get();
            return ResponseEntity.ok(new UrlStatsResponse(
                url.getUrl(),
                baseUrl + url.getShortUrl(),
                url.getClickCount(),
                url.getCreatedAt(),
                url.getExpiresAt()
            ));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Updates the expiration date of a short URL.
     *
     * @param shortUrl       The short URL to update
     * @param expirationDays New number of days until expiration
     * @return ResponseEntity with the updated URL information (clickCount is a Long)
     */
    @Operation(summary = "Update URL expiration", description = "Updates the expiration date of an existing shortened URL", parameters = {
            @Parameter(name = "shortUrl", description = "The short URL to update", example = "abc123", required = true),
            @Parameter(name = "expirationDays", description = "New number of days until expiration", example = "14", required = true) })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL updated successfully", content = @Content(schema = @Schema(implementation = UrlShortenResponse.class), examples = {
                    @ExampleObject(name = "Success Response", value = "{\"code\": \"abc123\", \"shortUrl\": \"http://localhost:8080/api/v1/urls/abc123\", \"url\": \"https://example.com\", \"createdAt\": \"2024-05-10T12:00:00\", \"expiresAt\": \"2024-05-24T12:00:00\", \"clickCount\": 5}") })),
            @ApiResponse(responseCode = "404", description = "URL not found, expired, or disabled", content = @Content(schema = @Schema(type = "string"), examples = {
                    @ExampleObject(name = "Error Response", value = "URL not found") })),
            @ApiResponse(responseCode = "400", description = "Invalid expiration days", content = @Content(schema = @Schema(type = "string"), examples = {
                    @ExampleObject(name = "Error Response", value = "Expiration days must be between 1 and 365") })) })
    @PutMapping("/{shortUrl}")
    public ResponseEntity<UrlShortenResponse> updateUrlExpiration(
            @Parameter(description = "The short URL to update", required = true) @PathVariable String shortUrl,
            @Parameter(description = "New number of days until expiration", required = true) @RequestParam @Min(1) Integer expirationDays) {
        try {
            UrlGenerator.validateExpirationDays(expirationDays);
            return urlService.updateUrl(shortUrl, expirationDays)
                .map(url -> UrlShortenResponse.builder()
                    .shortUrl(url.getShortUrl())
                    .originalUrl(url.getUrl())
                    .createdAt(url.getCreatedAt())
                    .expiresAt(url.getExpiresAt())
                    .clickCount(url.getClickCount())
                    .status(url.getStatus())
                    .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (InvalidUrlException e) {
            return ResponseEntity.badRequest().body(new UrlShortenResponse(e.getMessage()));
        }
    }

    /**
     * Updates the status (enable/disable) of a URL.
     *
     * @param shortUrl The short URL to update
     * @param action   The action to perform (enable/disable)
     * @return UrlShortenResponse containing the updated short URL and expiration date (clickCount is a Long)
     */
    @Operation(summary = "Update URL Status", description = "Updates the status (enable/disable) of a URL", parameters = {
            @Parameter(name = "shortUrl", description = "The short URL to update", example = "abc123", required = true),
            @Parameter(name = "action", description = "The action to perform (enable/disable)", example = "enable", required = true) })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL status updated successfully", content = @Content(schema = @Schema(implementation = UrlShortenResponse.class), examples = {
                    @ExampleObject(name = "Success Response", value = "{\"code\": \"abc123\", \"shortUrl\": \"http://localhost:8080/api/v1/urls/abc123\", \"url\": \"https://example.com\", \"createdAt\": \"2024-05-10T12:00:00\", \"expiresAt\": \"2024-05-10T12:00:00\", \"clickCount\": 5}") })),
            @ApiResponse(responseCode = "400", description = "Invalid action provided", content = @Content(schema = @Schema(type = "string"), examples = {
                    @ExampleObject(name = "Error Response", value = "Invalid action. Must be either 'enable' or 'disable'") })),
            @ApiResponse(responseCode = "404", description = "URL not found", content = @Content(schema = @Schema(type = "string"), examples = {
                    @ExampleObject(name = "Error Response", value = "URL not found") })) })
    @PostMapping("/{shortUrl}/{action}")
    public ResponseEntity<UrlShortenResponse> updateUrlStatus(@PathVariable String shortUrl,
            @PathVariable String action) {
        try {
            return urlService.updateUrlStatus(shortUrl, action)
                .map(url -> UrlShortenResponse.builder()
                    .shortUrl(url.getShortUrl())
                    .originalUrl(url.getUrl())
                    .createdAt(url.getCreatedAt())
                    .expiresAt(url.getExpiresAt())
                    .clickCount(url.getClickCount())
                    .status(url.getStatus())
                    .build())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (InvalidUrlException e) {
            return ResponseEntity.badRequest().body(new UrlShortenResponse(e.getMessage()));
        }
    }
}