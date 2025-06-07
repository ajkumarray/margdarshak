package com.ajkumarray.margdarshak.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMasterRequest {

    @JsonProperty("name")
    @Schema(description = "The name of the user")
    private String name;

    @JsonProperty("email")
    @Schema(description = "The email of the user")
    private String email;

    @JsonProperty("password")
    @Schema(description = "The password of the user")
    private String password;

    @JsonProperty("profilePic")
    @Schema(description = "The profile picture of the user")
    private String profilePic;

}
