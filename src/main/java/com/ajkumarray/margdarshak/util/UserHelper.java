package com.ajkumarray.margdarshak.util;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ajkumarray.margdarshak.entity.UserMasterEntity;
import com.ajkumarray.margdarshak.models.request.UserMasterRequest;
import com.ajkumarray.margdarshak.models.response.UserMasterResponse;
import com.ajkumarray.margdarshak.models.response.AuthResponse;
import com.ajkumarray.margdarshak.enums.UserStatusEnums;
import com.ajkumarray.margdarshak.security.JwtTokenProvider;

@Component
public class UserHelper {

    @Value("${user.password.salt}")
    private String passwordSalt;

    @Autowired
    private CommonFunctionHelper commonFunctionHelper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public UserMasterEntity prepareUserEntity(UserMasterRequest request) {
        UserMasterEntity userEntity = new UserMasterEntity();
        userEntity.setUserCode(commonFunctionHelper.generateAlphaNumericCode(8));
        userEntity.setName(request.getName());
        userEntity.setEmail(request.getEmail());
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userEntity.setProfilePic(request.getProfilePic());
        userEntity.setStatus(UserStatusEnums.ACTIVE);
        userEntity.setDeleted(false);
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());
        userEntity.setDeletedAt(null);
        return userEntity;
    }

    public UserMasterResponse prepareUserResponse(UserMasterEntity userEntity) {
        UserMasterResponse userResponse = new UserMasterResponse();
        userResponse.setUserCode(userEntity.getUserCode());
        userResponse.setName(userEntity.getName());
        userResponse.setEmail(userEntity.getEmail());
        userResponse.setProfilePic(userEntity.getProfilePic());
        userResponse.setStatus(userEntity.getStatus());
        userResponse.setCreatedAt(userEntity.getCreatedAt());
        userResponse.setUpdatedAt(userEntity.getUpdatedAt());
        return userResponse;
    }

    public AuthResponse prepareAuthResponse(UserMasterEntity userEntity) {
        AuthResponse authResponse = new AuthResponse();
        String token = jwtTokenProvider.generateToken(userEntity.getUserCode());
        authResponse.setToken(token);
        return authResponse;
    }

}
