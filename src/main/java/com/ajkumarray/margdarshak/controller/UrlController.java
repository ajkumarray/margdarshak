package com.ajkumarray.margdarshak.controller;

import java.util.Map;
import java.util.Optional;

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

import com.ajkumarray.margdarshak.dto.UrlCreateRequest;
import com.ajkumarray.margdarshak.dto.UrlShortenResponse;
import com.ajkumarray.margdarshak.dto.UrlStatsResponse;
import com.ajkumarray.margdarshak.entity.Url;
import com.ajkumarray.margdarshak.exception.InvalidUrlException;
import com.ajkumarray.margdarshak.service.UrlService;
import com.ajkumarray.margdarshak.validator.UrlValidator;

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

    /**
     * Creates a short URL for the given original URL.
     *
     * @param request The URL creation request containing the original URL and
     *                expiration days
     * @return ResponseEntity with the created short URL and expiration date
     * @throws InvalidUrlException if the URL is invalid or expiration days are
     *                             invalid
     */
    @Operation(summary = "Create a short URL", description = "Creates a new short URL for the given original URL with specified expiration days.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UrlCreateRequest.class), examples = {
            @ExampleObject(name = "Valid Request", value = "{\"url\": \"https://example.com\", \"expirationDays\": 30}") })))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL shortened successfully", content = @Content(schema = @Schema(implementation = UrlShortenResponse.class), examples = {
                    @ExampleObject(name = "Success Response", value = "{\"shortUrl\": \"abc123\", \"expiresAt\": \"2024-05-10T12:00:00\"}") })),
            @ApiResponse(responseCode = "400", description = "Invalid URL or expiration days", content = @Content(schema = @Schema(implementation = UrlShortenResponse.class), examples = {
                    @ExampleObject(name = "Error Response", value = "{\"errorMessage\": \"Invalid URL format\"}") })),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = UrlShortenResponse.class), examples = {
                    @ExampleObject(name = "Error Response", value = "{\"errorMessage\": \"Failed to generate unique short URL. Please try again.\"}") })) })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UrlShortenResponse> createShortUrl(@Valid @RequestBody UrlCreateRequest request) {
        try {
            urlValidator.validateUrl(request.getUrl(), request.getExpirationDays());
            Url url = urlService.createShortUrl(request.getUrl(), request.getExpirationDays());
            return ResponseEntity.ok(new UrlShortenResponse(url.getShortUrl(), url.getExpiresAt()));
        } catch (InvalidUrlException e) {
            return ResponseEntity.badRequest().body(new UrlShortenResponse(null, null, e.getMessage()));
        } catch (RuntimeException e) {
            String errorMessage = "Failed to generate unique short URL. Please try again.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UrlShortenResponse(null, null, errorMessage));
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
    public ResponseEntity<String> redirectToOriginalUrl(
            @Parameter(description = "The short URL to redirect from", required = true) @PathVariable String shortUrl) {
        return urlService.getOriginalUrl(shortUrl).map(url -> ResponseEntity.ok().body(url))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Gets information about a short URL.
     *
     * @param shortUrl The short URL to get information for
     * @return ResponseEntity with URL information if found and active
     */
    @Operation(summary = "Get URL information", description = "Retrieves basic information about a short URL")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "URL information retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "URL not found, expired, or disabled") })
    @GetMapping("/{shortUrl}/info")
    public ResponseEntity<?> getUrlInfo(
            @Parameter(description = "The short URL to get information for", required = true) @PathVariable String shortUrl) {
        return urlService.getOriginalUrl(shortUrl)
                .map(originalUrl -> ResponseEntity.ok().body(Map.of("originalUrl", originalUrl, "shortUrl", shortUrl)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Gets statistics for a short URL.
     *
     * @param shortUrl The short URL to get statistics for
     * @return ResponseEntity with URL statistics if found and active
     */
    @Operation(summary = "Get URL statistics", description = "Retrieves detailed statistics for a short URL including click count and timestamps", parameters = {
            @Parameter(name = "shortUrl", description = "The short URL to get statistics for", example = "abc123", required = true) })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully", content = @Content(schema = @Schema(implementation = UrlStatsResponse.class), examples = {
                    @ExampleObject(name = "Success Response", value = "{\"originalUrl\": \"https://example.com\", \"shortUrl\": \"abc123\", \"clickCount\": 10, \"createdAt\": \"2024-04-10T12:00:00\", \"expiresAt\": \"2024-05-10T12:00:00\"}") })),
            @ApiResponse(responseCode = "404", description = "URL not found, expired, or disabled", content = @Content(schema = @Schema(type = "string"), examples = {
                    @ExampleObject(name = "Error Response", value = "URL not found") })) })
    @GetMapping("/{shortUrl}/stats")
    public ResponseEntity<UrlStatsResponse> getUrlStats(
            @Parameter(description = "The short URL to get statistics for", required = true) @PathVariable String shortUrl) {
        Optional<Url> urlOptional = urlService.getUrlStats(shortUrl);
        if (urlOptional.isPresent()) {
            Url url = urlOptional.get();
            return ResponseEntity.ok(new UrlStatsResponse(url.getOriginalUrl(), url.getShortUrl(), url.getClickCount(),
                    url.getCreatedAt(), url.getExpiresAt()));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Updates the expiration date of a short URL.
     *
     * @param shortUrl       The short URL to update
     * @param expirationDays New number of days until expiration
     * @return ResponseEntity with the updated URL information
     */
    @Operation(summary = "Update URL expiration", description = "Updates the expiration date of an existing shortened URL", parameters = {
            @Parameter(name = "shortUrl", description = "The short URL to update", example = "abc123", required = true),
            @Parameter(name = "expirationDays", description = "New number of days until expiration", example = "14", required = true) })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL updated successfully", content = @Content(schema = @Schema(implementation = UrlShortenResponse.class), examples = {
                    @ExampleObject(name = "Success Response", value = "{\"shortUrl\": \"abc123\", \"expiresAt\": \"2024-05-24T12:00:00\"}") })),
            @ApiResponse(responseCode = "404", description = "URL not found, expired, or disabled", content = @Content(schema = @Schema(type = "string"), examples = {
                    @ExampleObject(name = "Error Response", value = "URL not found") })),
            @ApiResponse(responseCode = "400", description = "Invalid expiration days", content = @Content(schema = @Schema(type = "string"), examples = {
                    @ExampleObject(name = "Error Response", value = "Expiration days must be between 1 and 365") })) })
    @PutMapping("/{shortUrl}")
    public ResponseEntity<UrlShortenResponse> updateUrlExpiration(
            @Parameter(description = "The short URL to update", required = true) @PathVariable String shortUrl,
            @Parameter(description = "New number of days until expiration", required = true) @RequestParam @Min(1) Integer expirationDays) {
        return urlService.updateUrl(shortUrl, expirationDays)
                .map(url -> ResponseEntity.ok(new UrlShortenResponse(url.getShortUrl(), url.getExpiresAt())))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates the status (enable/disable) of a URL.
     *
     * @param shortUrl The short URL to update
     * @param action   The action to perform (enable/disable)
     * @return UrlShortenResponse containing the updated short URL and expiration
     *         date
     */
    @Operation(summary = "Update URL Status", description = "Updates the status (enable/disable) of a URL", parameters = {
            @Parameter(name = "shortUrl", description = "The short URL to update", example = "abc123", required = true),
            @Parameter(name = "action", description = "The action to perform (enable/disable)", example = "enable", required = true) })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL status updated successfully", content = @Content(schema = @Schema(implementation = UrlShortenResponse.class), examples = {
                    @ExampleObject(name = "Success Response", value = "{\"shortUrl\": \"abc123\", \"expiresAt\": \"2024-05-10T12:00:00\"}") })),
            @ApiResponse(responseCode = "400", description = "Invalid action provided", content = @Content(schema = @Schema(type = "string"), examples = {
                    @ExampleObject(name = "Error Response", value = "Invalid action. Must be either 'enable' or 'disable'") })),
            @ApiResponse(responseCode = "404", description = "URL not found", content = @Content(schema = @Schema(type = "string"), examples = {
                    @ExampleObject(name = "Error Response", value = "URL not found") })) })
    @PostMapping("/{shortUrl}/{action}")
    public ResponseEntity<UrlShortenResponse> updateUrlStatus(@PathVariable String shortUrl,
            @PathVariable String action) {
        if (!action.equalsIgnoreCase("enable") && !action.equalsIgnoreCase("disable")) {
            throw new IllegalArgumentException("Invalid action. Must be either 'enable' or 'disable'");
        }

        boolean enable = action.equalsIgnoreCase("enable");
        Optional<Url> urlOptional = enable ? urlService.enableUrl(shortUrl) : urlService.disableUrl(shortUrl);

        return urlOptional.map(url -> ResponseEntity.ok(new UrlShortenResponse(url.getShortUrl(), url.getExpiresAt())))
                .orElse(ResponseEntity.notFound().build());
    }
}