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
public class UrlStatsResponse {
    private String originalUrl;
    private String shortUrl;
    private Integer clickCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
} 