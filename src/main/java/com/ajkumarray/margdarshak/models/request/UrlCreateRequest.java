package com.ajkumarray.margdarshak.models.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlCreateRequest {
    @NotBlank(message = "URL is required")
    @Pattern(regexp = "^https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$", 
            message = "Invalid URL format")
    private String url;

    @Min(value = 1, message = "Expiration days must be at least 1")
    private Integer expirationDays;
} 