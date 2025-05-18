package com.ajkumarray.margdarshak.models.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlStatsResponse {
    private String url;
    private String shortUrl;
    /**
     * The number of times this short URL has been accessed (Long).
     */
    private Long clickCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
} 