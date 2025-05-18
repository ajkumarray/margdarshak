package com.ajkumarray.margdarshak.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ajkumarray.margdarshak.models.request.UserSignupRequest;
import com.ajkumarray.margdarshak.models.response.UserResponse;
import com.ajkumarray.margdarshak.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST Controller for handling user-related operations.
 * Provides endpoints for user management including registration and authentication.
 * All endpoints are prefixed with '/api/v1/users'.
 *
 * @author Ajit Kumar
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(
    name = "User Management",
    description = "APIs for managing user operations including registration and authentication"
)
public class UserController {

    /**
     * Service for handling user-related business logic.
     * Injected using field injection.
     */
    @Autowired
    private UserService userService;

    /**
     * Endpoint for user registration.
     * Creates a new user account with the provided details.
     * Validates the input data and ensures email uniqueness.
     *
     * @param request The user signup request containing user details
     * @return ResponseEntity containing the created user's information
     * @throws DuplicateResourceException if the email is already registered
     * @throws IllegalArgumentException if the request contains invalid data
     */
    @Operation(
        summary = "Register a new user",
        description = """
            Creates a new user account with the provided details.
            The email must be unique and the password must meet the following criteria:
            - At least 8 characters long
            - Contains at least one digit
            - Contains at least one uppercase letter
            - Contains at least one lowercase letter
            - Contains at least one special character
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User successfully registered",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data - validation failed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        )
    })
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(
            @Parameter(
                description = "User signup details",
                required = true,
                schema = @Schema(implementation = UserSignupRequest.class)
            )
            @Valid @RequestBody UserSignupRequest request) {
        /**
         * Process the signup request and return the created user's information.
         * The response is wrapped in a ResponseEntity for better HTTP control.
         */
        return ResponseEntity.ok(userService.signup(request));
    }
} 