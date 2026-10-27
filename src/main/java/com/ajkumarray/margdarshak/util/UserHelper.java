package com.ajkumarray.margdarshak.util;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ajkumarray.margdarshak.entity.RefreshTokenEntity;
import com.ajkumarray.margdarshak.entity.UserMasterEntity;
import com.ajkumarray.margdarshak.models.request.UserMasterRequest;
import com.ajkumarray.margdarshak.models.response.UserMasterResponse;
import com.ajkumarray.margdarshak.models.response.AuthResponse;
import com.ajkumarray.margdarshak.enums.UserRoleEnums;
import com.ajkumarray.margdarshak.enums.UserStatusEnums;
import com.ajkumarray.margdarshak.enums.UserVerificationStatusEnums;
import com.ajkumarray.margdarshak.security.JwtTokenProvider;

@Component
public class UserHelper {

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
        userEntity.setVerificationStatus(UserVerificationStatusEnums.PENDING);
        /*
         * Registration always creates a USER; admins are promoted manually in the DB.
         */
        userEntity.setRole(UserRoleEnums.USER);
        userEntity.setDeleted(false);
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setUpdatedAt(LocalDateTime.now());
        userEntity.setDeletedAt(null);
        return userEntity;
    }

    public UserMasterResponse prepareUserResponse(UserMasterEntity userEntity) {
        UserMasterResponse userResponse = new UserMasterResponse();
        userResponse.setName(userEntity.getName());
        userResponse.setEmail(userEntity.getEmail());
        userResponse.setProfilePic(userEntity.getProfilePic());
        userResponse.setStatus(userEntity.getStatus());
        userResponse.setVerificationStatus(userEntity.getVerificationStatus());
        userResponse.setRole(userEntity.getRole());
        return userResponse;
    }

    public AuthResponse prepareAuthResponse(UserMasterEntity userEntity, String refreshToken) {
        AuthResponse authResponse = new AuthResponse();
        String token = jwtTokenProvider.generateToken(userEntity.getUserCode(), userEntity.getRole());
        authResponse.setUserDetails(prepareUserResponse(userEntity));
        authResponse.setToken(token);
        authResponse.setRefreshToken(refreshToken);
        return authResponse;
    }

    public RefreshTokenEntity prepareRefreshTokenEntity(String userCode, String jti, LocalDateTime expiresAt) {
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setJti(jti);
        refreshTokenEntity.setUserCode(userCode);
        refreshTokenEntity.setExpiresAt(expiresAt);
        refreshTokenEntity.setDeleted(false);
        refreshTokenEntity.setCreatedAt(LocalDateTime.now());
        refreshTokenEntity.setUpdatedAt(LocalDateTime.now());
        refreshTokenEntity.setDeletedAt(null);
        return refreshTokenEntity;
    }

    public UserMasterEntity prepareUserUpdateEntity(UserMasterEntity userEntity, UserMasterRequest request) {
        if (!commonFunctionHelper.isEmptyOrBlank(request.getName())) {
            userEntity.setName(request.getName());
        }
        if (!commonFunctionHelper.isEmptyOrBlank(request.getEmail())) {
            userEntity.setEmail(request.getEmail());
        }
        if (!commonFunctionHelper.isEmptyOrBlank(request.getProfilePic())) {
            userEntity.setProfilePic(request.getProfilePic());
        }
        userEntity.setUpdatedAt(LocalDateTime.now());
        return userEntity;
    }

}
