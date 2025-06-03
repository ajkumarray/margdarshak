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

@RestController
@RequestMapping(value = "${spring.api}")
@Tag(name = "User", description = "User API")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping(value = "/register")
    public ResponseEntity<ObjectResponse> register(@RequestBody UserMasterRequest request) {
        ObjectResponse response = new ObjectResponse();
        response.setMessageCode(ApplicationEnums.URL_FAILED_CODE.getCode());
        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_FAILED_CODE.getCode()));
        HttpStatus headerStatus = HttpStatus.BAD_REQUEST;
        response.setList(null);

        Object result = userService.register(request);
        if (result != null) {
            response.setMessageCode(ApplicationEnums.URL_SUCCESS_CODE.getCode());
            response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_SUCCESS_CODE.getCode()));
            headerStatus = HttpStatus.OK;
            response.setList(result);
        }

        return new ResponseEntity<>(response, headerStatus);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<ObjectResponse> login(@RequestBody UserLoginRequest request) {
        ObjectResponse response = new ObjectResponse();
        response.setMessageCode(ApplicationEnums.URL_FAILED_CODE.getCode());
        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_FAILED_CODE.getCode()));
        HttpStatus headerStatus = HttpStatus.BAD_REQUEST;

        Object result = userService.login(request);
        if (result != null) {
            response.setMessageCode(ApplicationEnums.URL_SUCCESS_CODE.getCode());
            response.setMessage(MessageTranslator.toLocale(ApplicationEnums.URL_SUCCESS_CODE.getCode()));
            headerStatus = HttpStatus.OK;
            response.setList(result);
        }

        return new ResponseEntity<>(response, headerStatus);
    }

}