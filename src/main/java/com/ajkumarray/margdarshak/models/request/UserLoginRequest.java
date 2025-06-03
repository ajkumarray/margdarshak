package com.ajkumarray.margdarshak.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

    @JsonProperty("email")
    @Schema(description = "The email of the user")
    private String email;

    @JsonProperty("password")
    @Schema(description = "The password of the user")
    private String password;
}
