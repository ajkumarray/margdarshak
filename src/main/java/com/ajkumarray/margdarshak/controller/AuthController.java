package com.ajkumarray.margdarshak.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.models.request.UserMasterRequest;
import com.ajkumarray.margdarshak.models.request.UserLoginRequest;
import com.ajkumarray.margdarshak.models.response.ObjectResponse;
import com.ajkumarray.margdarshak.service.UserService;
import com.ajkumarray.margdarshak.util.MessageTranslator;
import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

/**
 * Controller for handling user authentication operations. Provides endpoints
 * for user registration and login.
 */
@RestController
@RequestMapping(value = "${spring.api}auth")
@Tag(name = "Authentication", description = "User authentication and registration APIs")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Registers a new user in the system.
     *
     * @param request The user registration request containing user details
     * @return ResponseEntity containing the registration result
     */
    @Operation(summary = "Register User", description = "Creates a new user account in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered", content = @Content(schema = @Schema(implementation = ObjectResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "409", description = "User already exists") })
    @PostMapping(value = "/register")
    public ResponseEntity<ObjectResponse> register(
            @Parameter(description = "User registration details") @RequestBody UserMasterRequest request) {
        ObjectResponse response = new ObjectResponse();
        HttpStatus headerStatus = HttpStatus.OK;
        response.setMessageCode(ApplicationEnums.SIGNUP_SUCCESS.getCode());
        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.SIGNUP_SUCCESS.getCode()));

        Object result = userService.register(request);
        response.setList(result);

        return new ResponseEntity<>(response, headerStatus);
    }

    /**
     * Authenticates a user and provides access token.
     *
     * @param request The login request containing user credentials
     * @return ResponseEntity containing the authentication result and JWT token
     */
    @Operation(summary = "User Login", description = "Authenticates user credentials and returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = ObjectResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters") })
    @PostMapping(value = "/login")
    public ResponseEntity<ObjectResponse> login(
            @Parameter(description = "User login credentials") @RequestBody UserLoginRequest request) {
        ObjectResponse response = new ObjectResponse();
        HttpStatus headerStatus = HttpStatus.OK;
        response.setMessageCode(ApplicationEnums.LOGIN_SUCCESS.getCode());
        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.LOGIN_SUCCESS.getCode()));

        Object result = userService.login(request);
        response.setList(result);

        return new ResponseEntity<>(response, headerStatus);
    }
}