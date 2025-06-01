package com.ajkumarray.margdarshak.models.response;

import java.time.LocalDateTime;

import com.ajkumarray.margdarshak.enums.UrlStatusEnums;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrlMasterResponse {

    @JsonProperty("id")
    @Schema(description = "id: id of the url")
    private long id;

    @JsonProperty("code")
    @Schema(description = "code: code of the url")
    private String code;

    @JsonProperty("shortUrl")
    @Schema(description = "shortUrl: short url of the url")
    private String shortUrl;

    @JsonProperty("url")
    @Schema(description = "url: original url of the url")
    private String url;

    @JsonProperty("createdAt")
    @Schema(description = "createdAt: created at of the url")
    private LocalDateTime createdAt;

    @JsonProperty("expiresAt")
    @Schema(description = "expiresAt: expires at of the url")
    private LocalDateTime expiresAt;

    @JsonProperty("clickCount")
    @Schema(description = "clickCount: click count of the url")
    private Long clickCount;

    @JsonProperty("status")
    @Schema(description = "status: status of the url")
    private UrlStatusEnums status;

    @JsonProperty("lastAccessedAt")
    @Schema(description = "lastAccessedAt: last accessed at of the url")
    private LocalDateTime lastAccessedAt;

    @JsonProperty("createdBy")
    @Schema(description = "createdBy: created by of the url")
    private String createdBy;

}