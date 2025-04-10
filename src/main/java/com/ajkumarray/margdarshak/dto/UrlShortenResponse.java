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
    private String shortUrl;
    private LocalDateTime expiresAt;
    private String errorMessage;

    public UrlShortenResponse(String shortUrl, LocalDateTime expiresAt) {
        this.shortUrl = shortUrl;
        this.expiresAt = expiresAt;
        this.errorMessage = null;
    }
} 