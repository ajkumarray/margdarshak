package com.ajkumarray.margdarshak.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.models.request.UserMasterRequest;
import com.ajkumarray.margdarshak.models.response.ObjectResponse;
import com.ajkumarray.margdarshak.service.UserService;
import com.ajkumarray.margdarshak.util.MessageTranslator;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping(value = "${spring.api}user")
@Tag(name = "User", description = "User related APIs")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userCode}")
    public ResponseEntity<ObjectResponse> getUserDetails(
            @Parameter(description = "User code") @PathVariable String userCode) {
        ObjectResponse response = new ObjectResponse();
        HttpStatus headerStatus = HttpStatus.OK;
        response.setMessageCode(ApplicationEnums.SUCCESS_MESSAGE.getCode());
        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.SUCCESS_MESSAGE.getCode()));

        Object result = userService.getUserDetails(userCode);
        response.setList(result);

        return new ResponseEntity<>(response, headerStatus);
    }

    @PutMapping("/{userCode}")
    public ResponseEntity<ObjectResponse> updateUserDetails(
            @Parameter(description = "User code") @PathVariable String userCode,
            @Parameter(description = "User details") @RequestBody UserMasterRequest request) {
        ObjectResponse response = new ObjectResponse();
        HttpStatus headerStatus = HttpStatus.OK;
        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.SUCCESS_MESSAGE.getCode()));
        response.setMessageCode(ApplicationEnums.SUCCESS_MESSAGE.getCode());

        Object result = userService.updateUserDetails(userCode, request);
        response.setList(result);

        return new ResponseEntity<>(response, headerStatus);
    }

}
