package com.ajkumarray.margdarshak.service;

import com.ajkumarray.margdarshak.models.request.UserMasterRequest;
import com.ajkumarray.margdarshak.models.request.UserLoginRequest;
import com.ajkumarray.margdarshak.models.response.AuthResponse;

import org.springframework.stereotype.Service;

@Service
public interface UserService {

    AuthResponse register(UserMasterRequest request);

    AuthResponse login(UserLoginRequest request);

}
