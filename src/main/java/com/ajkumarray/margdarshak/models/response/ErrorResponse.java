package com.ajkumarray.margdarshak.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {

    @JsonProperty("errorCode")
    @Schema(description = "errorCode : Error code")
    private String errorCode;

    @JsonProperty("errorMessage")
    @Schema(description = "errorMessage : Error message")
    private String errorMessage;

}
