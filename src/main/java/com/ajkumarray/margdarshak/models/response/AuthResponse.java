package com.ajkumarray.margdarshak.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {

    @JsonProperty("token")
    @Schema(description = "The token of the user")
    private String token;

    @JsonProperty("userDetails")
    @Schema(description = "The user details of the user")
    private UserMasterResponse userDetails;

}
