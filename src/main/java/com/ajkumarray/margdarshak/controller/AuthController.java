package com.ajkumarray.margdarshak.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.models.request.UserMasterRequest;
import com.ajkumarray.margdarshak.models.request.UserLoginRequest;
import com.ajkumarray.margdarshak.models.response.ObjectResponse;
import com.ajkumarray.margdarshak.service.UserService;
import com.ajkumarray.margdarshak.util.MessageTranslator;
import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping(value = "${spring.api}auth")
@Tag(name = "User", description = "User API")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping(value = "/register")
    public ResponseEntity<ObjectResponse> register(@RequestBody UserMasterRequest request) {
        ObjectResponse response = new ObjectResponse();
        HttpStatus headerStatus = HttpStatus.OK;
        response.setMessageCode(ApplicationEnums.SIGNUP_SUCCESS.getCode());
        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.SIGNUP_SUCCESS.getCode()));

        Object result = userService.register(request);
        response.setList(result);

        return new ResponseEntity<>(response, headerStatus);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<ObjectResponse> login(@RequestBody UserLoginRequest request) {
        ObjectResponse response = new ObjectResponse();
        HttpStatus headerStatus = HttpStatus.OK;
        response.setMessageCode(ApplicationEnums.LOGIN_SUCCESS.getCode());
        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.LOGIN_SUCCESS.getCode()));

        Object result = userService.login(request);
        response.setList(result);

        return new ResponseEntity<>(response, headerStatus);
    }

}