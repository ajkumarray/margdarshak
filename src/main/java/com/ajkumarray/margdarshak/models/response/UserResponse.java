package com.ajkumarray.margdarshak.models.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for user responses.
 * Contains user information to be returned to clients.
 * This class is used to transfer user data from the server to the client.
 *
 * @author Ajit Kumar
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    /**
     * Unique identifier for the user.
     * Generated during user creation.
     * Used as a reference for user operations.
     * 
     * @example "ABC12345"
     */
    private String userCode;

    /**
     * User's full name.
     * Used for display purposes.
     * 
     * @example "John Doe"
     */
    private String name;

    /**
     * User's email address.
     * Used for authentication and communication.
     * 
     * @example "john.doe@example.com"
     */
    private String email;

    /**
     * URL to the user's profile picture.
     * May be null if no profile picture is set.
     * Used for displaying user's profile image.
     * 
     * @example "https://example.com/profile.jpg"
     */
    private String profilePic;

    /**
     * Timestamp when the user was created.
     * Automatically set during user creation.
     * Used for auditing purposes.
     * 
     * @example "2024-05-18T21:21:19.337525802"
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user was last updated.
     * Automatically updated when user data changes.
     * Used for auditing purposes.
     * 
     * @example "2024-05-18T21:21:19.337525802"
     */
    private LocalDateTime updatedAt;

    /**
     * Error message if any error occurred during the operation.
     * Null if the operation was successful.
     * Used for error handling and client feedback.
     * 
     * @example "Email already registered"
     */
    private String errorMessage;
} 