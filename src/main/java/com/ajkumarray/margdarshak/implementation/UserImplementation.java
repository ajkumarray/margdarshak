package com.ajkumarray.margdarshak.implementation;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ajkumarray.margdarshak.models.request.UserMasterRequest;
import com.ajkumarray.margdarshak.models.request.UserLoginRequest;
import com.ajkumarray.margdarshak.service.UserService;
import com.ajkumarray.margdarshak.util.UserHelper;
import com.ajkumarray.margdarshak.repository.UserRepository;
import com.ajkumarray.margdarshak.entity.UserMasterEntity;
import com.ajkumarray.margdarshak.util.CommonFunctionHelper;
import com.ajkumarray.margdarshak.exception.ApplicationException;
import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.util.MessageTranslator;
import com.ajkumarray.margdarshak.models.response.AuthResponse;

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
            return userHelper.prepareAuthResponse(userEntity);
        } catch (ApplicationException e) {
            commonFunctionHelper.commonLoggerHelper(e, "UserImplementation -> register failed");
            throw e;
        } catch (Exception e) {
            commonFunctionHelper.commonLoggerHelper(e, "UserImplementation -> register failed");
            throw new ApplicationException(
                    MessageTranslator.toLocale(ApplicationEnums.SIGNUP_FAILED.getCode()),
                    ApplicationEnums.SIGNUP_FAILED.getCode());
        }
    }

    @Override
    public AuthResponse login(UserLoginRequest request) {
        try {
            Optional<UserMasterEntity> userEntity = userRepository.findByEmail(request.getEmail());
            if (userEntity.isPresent()) {
                if (passwordEncoder.matches(request.getPassword(), userEntity.get().getPassword())) {
                    return userHelper.prepareAuthResponse(userEntity.get());
                } else {
                    throw new ApplicationException(
                            MessageTranslator.toLocale(ApplicationEnums.LOGIN_FAILED.getCode()),
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

}
