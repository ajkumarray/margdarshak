package com.ajkumarray.margdarshak.implementation;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ajkumarray.margdarshak.models.request.UserMasterRequest;
import com.ajkumarray.margdarshak.models.request.UserLoginRequest;
import com.ajkumarray.margdarshak.service.UserService;
import com.ajkumarray.margdarshak.util.UserHelper;
import com.ajkumarray.margdarshak.repository.UserRepository;
import com.ajkumarray.margdarshak.repository.RefreshTokenRepository;
import com.ajkumarray.margdarshak.security.JwtTokenProvider;
import com.ajkumarray.margdarshak.entity.UserMasterEntity;
import com.ajkumarray.margdarshak.util.CommonFunctionHelper;
import com.ajkumarray.margdarshak.exception.ApplicationException;
import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.enums.UserStatusEnums;
import com.ajkumarray.margdarshak.util.MessageTranslator;
import com.ajkumarray.margdarshak.models.response.AuthResponse;
import com.ajkumarray.margdarshak.models.response.UserMasterResponse;

@Component
public class UserImplementation implements UserService {

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommonFunctionHelper commonFunctionHelper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthResponse register(UserMasterRequest request) {
        try {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ApplicationException(
                        MessageTranslator.toLocale(ApplicationEnums.USER_ALREADY_EXISTS.getCode()),
                        ApplicationEnums.USER_ALREADY_EXISTS.getCode());
            }
            UserMasterEntity userEntity = userHelper.prepareUserEntity(request);
            userEntity = userRepository.save(userEntity);
            return issueTokens(userEntity);
        } catch (ApplicationException e) {
            commonFunctionHelper.commonLoggerHelper(e, "UserImplementation -> register failed");
            throw e;
        } catch (Exception e) {
            commonFunctionHelper.commonLoggerHelper(e, "UserImplementation -> register failed");
            throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.SIGNUP_FAILED.getCode()),
                    ApplicationEnums.SIGNUP_FAILED.getCode());
        }
    }

    @Override
    public AuthResponse login(UserLoginRequest request) {
        try {
            Optional<UserMasterEntity> userEntity = userRepository.findByEmailAndStatusAndDeleted(request.getEmail(),
                    UserStatusEnums.ACTIVE, false);
            if (userEntity.isPresent()) {
                if (passwordEncoder.matches(request.getPassword(), userEntity.get().getPassword())) {
                    return issueTokens(userEntity.get());
                } else {
                    throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.LOGIN_FAILED.getCode()),
                            ApplicationEnums.LOGIN_FAILED.getCode());
                }
            } else {
                throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.LOGIN_FAILED.getCode()),
                        ApplicationEnums.LOGIN_FAILED.getCode());
            }
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            commonFunctionHelper.commonLoggerHelper(e, "UserImplementation -> login failed");
            throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.LOGIN_FAILED.getCode()),
                    ApplicationEnums.LOGIN_FAILED.getCode());
        }
    }

    @Override
    public AuthResponse refresh(String userCode, String jti) {
        try {
            /*
             * Atomic, so a refresh token works only once, even under concurrent use.
             */
            if (refreshTokenRepository.revoke(jti, userCode, LocalDateTime.now()) == 0) {
                throw invalidRefreshToken();
            }

            /*
             * Reloaded so role changes and blocked accounts take effect on refresh.
             */
            UserMasterEntity user = userRepository
                    .findByUserCodeAndStatusAndDeleted(userCode, UserStatusEnums.ACTIVE, false)
                    .orElseThrow(this::invalidRefreshToken);
            return issueTokens(user);
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            commonFunctionHelper.commonLoggerHelper(e, "UserImplementation -> refresh failed");
            throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.FAILED_MESSAGE.getCode()),
                    ApplicationEnums.FAILED_MESSAGE.getCode());
        }
    }

    @Override
    public void logout(String userCode, String jti, boolean all) {
        try {
            if (all) {
                /*
                 * Only an active session may end all sessions, not an old rotated token.
                 */
                if (!refreshTokenRepository.existsByJtiAndUserCodeAndDeleted(jti, userCode, false)) {
                    throw invalidRefreshToken();
                }
                refreshTokenRepository.revokeAllForUser(userCode, LocalDateTime.now());
            } else {
                refreshTokenRepository.revoke(jti, userCode, LocalDateTime.now());
            }
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            commonFunctionHelper.commonLoggerHelper(e, "UserImplementation -> logout failed");
            throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.FAILED_MESSAGE.getCode()),
                    ApplicationEnums.FAILED_MESSAGE.getCode());
        }
    }

    @Override
    public UserMasterResponse getUserDetails(String userCode) {
        try {
            Optional<UserMasterEntity> userEntity = userRepository.findByUserCodeAndStatusAndDeleted(userCode,
                    UserStatusEnums.ACTIVE, false);
            if (userEntity.isPresent()) {
                return userHelper.prepareUserResponse(userEntity.get());
            } else {
                throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.INVALID_USER_CODE.getCode()),
                        ApplicationEnums.INVALID_USER_CODE.getCode());
            }
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            commonFunctionHelper.commonLoggerHelper(e, "UserImplementation -> getUserDetails failed");
            throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.FAILED_MESSAGE.getCode()),
                    ApplicationEnums.FAILED_MESSAGE.getCode());
        }
    }

    @Override
    public UserMasterResponse updateUserDetails(String userCode, UserMasterRequest request) {
        try {
            Optional<UserMasterEntity> userEntity = userRepository.findByUserCodeAndStatusAndDeleted(userCode,
                    UserStatusEnums.ACTIVE, false);
            if (userEntity.isPresent()) {
                UserMasterEntity user = userHelper.prepareUserUpdateEntity(userEntity.get(), request);
                userRepository.save(user);
                return userHelper.prepareUserResponse(user);
            } else {
                throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.INVALID_USER_CODE.getCode()),
                        ApplicationEnums.INVALID_USER_CODE.getCode());
            }

        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            commonFunctionHelper.commonLoggerHelper(e, "UserImplementation -> updateUserDetails failed");
            throw new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.FAILED_MESSAGE.getCode()),
                    ApplicationEnums.FAILED_MESSAGE.getCode());
        }
    }

    /**
     * Starts a new session: saves its row and signs a refresh token with its jti.
     */
    private AuthResponse issueTokens(UserMasterEntity user) {
        String jti = UUID.randomUUID().toString();
        Date expiresAt = new Date(System.currentTimeMillis() + jwtTokenProvider.getRefreshExpiration() * 1000);
        refreshTokenRepository.save(userHelper.prepareRefreshTokenEntity(user.getUserCode(), jti,
                LocalDateTime.ofInstant(expiresAt.toInstant(), ZoneId.systemDefault())));
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserCode(), jti, expiresAt);
        return userHelper.prepareAuthResponse(user, refreshToken);
    }

    private ApplicationException invalidRefreshToken() {
        return new ApplicationException(MessageTranslator.toLocale(ApplicationEnums.INVALID_REFRESH_TOKEN.getCode()),
                ApplicationEnums.INVALID_REFRESH_TOKEN.getCode());
    }

}
