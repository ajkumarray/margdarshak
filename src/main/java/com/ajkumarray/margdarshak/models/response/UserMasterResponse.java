package com.ajkumarray.margdarshak.models.response;

import com.ajkumarray.margdarshak.enums.UserStatusEnums;
import com.ajkumarray.margdarshak.enums.UserVerificationStatusEnums;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserMasterResponse {

    @JsonProperty("name")
    @Schema(description = "The name of the user")
    private String name;

    @JsonProperty("email")
    @Schema(description = "The email of the user")
    private String email;

    @JsonProperty("profilePic")
    @Schema(description = "The profile picture of the user")
    private String profilePic;

    @JsonProperty("status")
    @Schema(description = "The status of the user")
    private UserStatusEnums status;

    @JsonProperty("verificationStatus")
    @Schema(description = "The email verification status of the user")
    private UserVerificationStatusEnums verificationStatus;

}
