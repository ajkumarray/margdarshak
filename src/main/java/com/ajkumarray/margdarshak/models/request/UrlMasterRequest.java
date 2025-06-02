package com.ajkumarray.margdarshak.models.request;

import com.ajkumarray.margdarshak.enums.UrlStatusEnums;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UrlMasterRequest {

    @JsonProperty("url")
    @Schema(description = "The original URL to shorten")
    private String url;

    @JsonProperty("expirationDays")
    @Schema(description = "The number of days until the URL expires")
    private Integer expirationDays;

    @JsonProperty("status")
    @Schema(description = "The status of the URL")
    private UrlStatusEnums status;

}