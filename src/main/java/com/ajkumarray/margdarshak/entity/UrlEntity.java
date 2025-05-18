package com.ajkumarray.margdarshak.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.ajkumarray.margdarshak.enums.UrlStatusEnums;

/**
 * Entity class representing a URL in the system.
 * Contains information about both the original and shortened URLs.
 */
@Entity
@Table(name = "urls")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Original URL cannot be empty")
    @Pattern(regexp = "^https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$", 
            message = "Invalid URL format")
    private String url;

    @Column(name = "short_url", nullable = false, unique = true)
    @NotBlank(message = "Short URL cannot be empty")
    private String shortUrl;

    @Column(name = "created_at", nullable = false)
    @NotNull(message = "Created at timestamp cannot be null")
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    @NotNull(message = "Expires at timestamp cannot be null")
    private LocalDateTime expiresAt;

    /**
     * The number of times this short URL has been accessed.
     * This is a Long value and is incremented on each redirect.
     */
    @Column(name = "click_count", nullable = false)
    @Min(value = 0, message = "Click count cannot be negative")
    private Long clickCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Status cannot be null")
    private UrlStatusEnums status;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
} 