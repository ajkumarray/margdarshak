package com.ajkumarray.margdarshak.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UrlShortenResponse {
    private String code;
    private String shortUrl;
    private String url;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long clickCount;
    private String errorMessage;

    public UrlShortenResponse(String shortCode, String shortUrl, String originalUrl, 
                            LocalDateTime createdAt, LocalDateTime expiresAt, Integer clickCount) {
        this.code = shortCode;
        this.shortUrl = shortUrl;
        this.url = originalUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.clickCount = clickCount.longValue();
        this.errorMessage = null;
    }

    public UrlShortenResponse(String errorMessage) {
        this.code = null;
        this.shortUrl = null;
        this.url = null;
        this.createdAt = null;
        this.expiresAt = null;
        this.clickCount = null;
        this.errorMessage = errorMessage;
    }
} 