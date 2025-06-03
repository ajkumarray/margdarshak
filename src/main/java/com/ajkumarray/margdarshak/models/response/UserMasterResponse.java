package com.ajkumarray.margdarshak.models.response;

import java.time.LocalDateTime;

import com.ajkumarray.margdarshak.enums.UserStatusEnums;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;
@Getter
@Setter
public class UserMasterResponse {

    @JsonProperty("userCode")
    @Schema(description = "The user code of the user")
    private String userCode;

    @JsonProperty("name")
    @Schema(description = "The name of the user")
    private String name;

    @JsonProperty("email")
    @Schema(description = "The email of the user")
    private String email;

    @JsonProperty("profilePic")
    @Schema(description = "The profile picture of the user")
    private String profilePic;

    @JsonProperty("createdAt")
    @Schema(description = "The created at of the user")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @Schema(description = "The updated at of the user")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonProperty("status")
    @Schema(description = "The status of the user")
    private UserStatusEnums status;
}
