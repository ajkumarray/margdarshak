package com.ajkumarray.margdarshak.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorListResponse extends ParentResponse {

    @JsonProperty("err")
    @Schema(description = "errorList : Error list")
    private ErrorResponse error;

}