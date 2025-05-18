package com.ajkumarray.margdarshak.models.response;

import java.time.LocalDateTime;

import com.ajkumarray.margdarshak.enums.UrlStatusEnums;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlShortenResponse {
    private String shortUrl;
    private String originalUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    /**
     * The number of times this short URL has been accessed (Long).
     */
    private Long clickCount;
    private UrlStatusEnums status;
    private String errorMessage;

    public UrlShortenResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
} 