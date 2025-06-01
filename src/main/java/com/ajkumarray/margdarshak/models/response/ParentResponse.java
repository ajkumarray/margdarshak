package com.ajkumarray.margdarshak.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ParentResponse {

    @JsonProperty("mc")
    @Schema(description = "mc: response message code")
    private String messageCode;

    @JsonProperty("m")
    @Schema(description = "m: response message")
    private String message;

}
