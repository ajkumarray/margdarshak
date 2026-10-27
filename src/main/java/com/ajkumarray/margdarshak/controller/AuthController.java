package com.ajkumarray.margdarshak.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.models.request.UserMasterRequest;
import com.ajkumarray.margdarshak.models.request.UserLoginRequest;
import com.ajkumarray.margdarshak.models.response.ObjectResponse;
import com.ajkumarray.margdarshak.security.AuthContextInterceptor;
import com.ajkumarray.margdarshak.security.RefreshTokenInterceptor;
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
    @Operation(summary = "Register User", description = "Creates a new user account with role USER and returns a JWT. A role in the request body is ignored.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered", content = @Content(schema = @Schema(implementation = ObjectResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters or user already exists") })
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
    @Operation(summary = "User Login", description = "Authenticates user credentials and returns a JWT carrying the user's role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = ObjectResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials") })
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

    /**
     * Issues a new access token and refresh token for a valid refresh token. The
     * refresh token used is revoked (rotation).
     *
     * @param userCode From the refresh token in "Authorization: Bearer ..."
     * @param jti      The session id carried by that refresh token
     * @return ResponseEntity containing the new tokens and user details
     */
    @Operation(summary = "Refresh Token", description = "Exchanges a refresh token for a new access token and refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed", content = @Content(schema = @Schema(implementation = ObjectResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid, expired or revoked refresh token") })
    @PostMapping(value = "/refresh")
    public ResponseEntity<ObjectResponse> refresh(
            @Parameter(hidden = true) @RequestAttribute(AuthContextInterceptor.USER_CODE) String userCode,
            @Parameter(hidden = true) @RequestAttribute(RefreshTokenInterceptor.JTI) String jti) {
        ObjectResponse response = new ObjectResponse();
        HttpStatus headerStatus = HttpStatus.OK;
        response.setMessageCode(ApplicationEnums.TOKEN_REFRESH_SUCCESS.getCode());
        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.TOKEN_REFRESH_SUCCESS.getCode()));

        Object result = userService.refresh(userCode, jti);
        response.setList(result);

        return new ResponseEntity<>(response, headerStatus);
    }

    /**
     * Ends the session the refresh token belongs to, or all of the user's
     * sessions when all is true.
     *
     * @param userCode From the refresh token in "Authorization: Bearer ..."
     * @param jti      The session id carried by that refresh token
     * @param all      If true, ends every session of the user
     * @return ResponseEntity confirming the logout
     */
    @Operation(summary = "Logout", description = "Ends the session of the given refresh token, or every session of the user when all=true")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out", content = @Content(schema = @Schema(implementation = ObjectResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token") })
    @PostMapping(value = "/logout")
    public ResponseEntity<ObjectResponse> logout(
            @Parameter(hidden = true) @RequestAttribute(AuthContextInterceptor.USER_CODE) String userCode,
            @Parameter(hidden = true) @RequestAttribute(RefreshTokenInterceptor.JTI) String jti,
            @Parameter(description = "End every session of the user instead of only this one") @RequestParam(defaultValue = "false") boolean all) {
        ObjectResponse response = new ObjectResponse();
        HttpStatus headerStatus = HttpStatus.OK;
        response.setMessageCode(ApplicationEnums.LOGOUT_SUCCESS.getCode());
        response.setMessage(MessageTranslator.toLocale(ApplicationEnums.LOGOUT_SUCCESS.getCode()));

        userService.logout(userCode, jti, all);

        return new ResponseEntity<>(response, headerStatus);
    }
}
