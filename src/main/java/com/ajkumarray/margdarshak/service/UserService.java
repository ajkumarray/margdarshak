package com.ajkumarray.margdarshak.service;

import org.springframework.stereotype.Service;

import com.ajkumarray.margdarshak.models.request.UserSignupRequest;
import com.ajkumarray.margdarshak.models.response.UserResponse;

/**
 * Service interface for user-related operations.
 * Provides methods for user management and authentication.
 * This service handles the business logic for user operations.
 *
 * @author Ajit Kumar
 * @version 1.0
 */
@Service
public interface UserService {
    
    /**
     * Creates a new user account.
     * This method performs the following operations:
     * 1. Validates the input data
     * 2. Checks for email uniqueness
     * 3. Encrypts the password
     * 4. Creates and saves the user entity
     * 5. Returns the user information
     * 
     * @param request The user signup request containing user details
     * @return UserResponse containing the created user's information
     * @throws DuplicateResourceException if the email is already registered
     * @throws IllegalArgumentException if the request contains invalid data
     */
    UserResponse signup(UserSignupRequest request);
} 