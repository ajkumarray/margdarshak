package com.ajkumarray.margdarshak.implementation;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ajkumarray.margdarshak.entity.UserEntity;
import com.ajkumarray.margdarshak.exception.DuplicateResourceException;
import com.ajkumarray.margdarshak.models.request.UserSignupRequest;
import com.ajkumarray.margdarshak.models.response.UserResponse;
import com.ajkumarray.margdarshak.repository.UserRepository;
import com.ajkumarray.margdarshak.service.UserService;

/**
 * Implementation of the UserService interface.
 * Handles user-related business logic including user registration.
 * This class is responsible for:
 * - User registration and validation
 * - Password encryption
 * - User entity management
 *
 * @author Ajit Kumar
 * @version 1.0
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     * 
     * Implementation details:
     * 1. Validates the input data using Jakarta validation
     * 2. Checks if the email is already registered
     * 3. Generates a unique user code
     * 4. Encrypts the user's password using BCrypt
     * 5. Creates a new user entity with default values
     * 6. Saves the user to the database
     * 7. Returns the user information in the response
     *
     * @param request The user signup request containing user details
     * @return UserResponse containing the created user's information
     * @throws DuplicateResourceException if the email is already registered
     */
    @Override
    public UserResponse signup(UserSignupRequest request) {
        /**
         * Check if the email is already registered in the system.
         * If found, throw a DuplicateResourceException.
         */
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        /**
         * Generate a unique user code for the new user.
         * This code will be used as the user's identifier.
         */
        String userCode = generateUniqueUserCode();

        /**
         * Create a new user entity with the provided details.
         * Set default values for required fields.
         * Encrypt the password before storing.
         */
        UserEntity user = UserEntity.builder()
                .userCode(userCode)
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .profilePic(request.getProfilePic())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        /**
         * Persist the user entity to the database.
         * The saved entity will have all generated fields populated.
         */
        user = userRepository.save(user);

        /**
         * Create and return the response object with user details.
         * Exclude sensitive information like password.
         */
        return UserResponse.builder()
                .userCode(user.getUserCode())
                .name(user.getName())
                .email(user.getEmail())
                .profilePic(user.getProfilePic())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Generates a unique user code.
     * The code is an 8-character uppercase string generated from UUID.
     * Ensures uniqueness by checking against existing codes in the database.
     *
     * @return A unique user code
     */
    private String generateUniqueUserCode() {
        String userCode;
        do {
            userCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (userRepository.existsByUserCode(userCode));
        return userCode;
    }
}