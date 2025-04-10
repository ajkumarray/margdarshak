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

@Entity
@Table(name = "urls")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Original URL cannot be empty")
    @Pattern(regexp = "^https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$", 
            message = "Invalid URL format")
    private String originalUrl;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Short URL cannot be empty")
    private String shortUrl;

    @Column(nullable = false)
    @NotNull(message = "Created at timestamp cannot be null")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @NotNull(message = "Expires at timestamp cannot be null")
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Min(value = 0, message = "Click count cannot be negative")
    private Integer clickCount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status cannot be null")
    private UrlStatus status;

    @Column
    private LocalDateTime lastAccessedAt;

    @Column
    private String createdBy;
} 