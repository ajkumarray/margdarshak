package com.ajkumarray.margdarshak.models.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for user signup requests.
 * Contains validation rules for user registration data.
 * This class is used to validate and transfer user registration data from the client to the server.
 *
 * @author Ajit Kumar
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupRequest {

    /**
     * User's full name.
     * Cannot be null or empty.
     * Used for display purposes.
     * 
     * @example "John Doe"
     */
    @NotBlank(message = "Name cannot be empty")
    private String name;

    /**
     * User's email address.
     * Must be a valid email format and cannot be null.
     * Used for authentication and communication.
     * Must be unique in the system.
     * 
     * @example "john.doe@example.com"
     */
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * User's password.
     * Must meet the following criteria:
     * - At least 8 characters long
     * - Contains at least one digit
     * - Contains at least one uppercase letter
     * - Contains at least one lowercase letter
     * - Contains at least one special character
     * Will be encrypted before storage.
     * 
     * @example "Test@123"
     */
    @NotBlank(message = "Password cannot be empty")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
        message = "Password must be at least 8 characters long and contain at least one digit, one uppercase letter, one lowercase letter, and one special character"
    )
    private String password;

    /**
     * URL to the user's profile picture.
     * Optional field.
     * Used for displaying user's profile image.
     * Should be a valid URL if provided.
     * 
     * @example "https://example.com/profile.jpg"
     */
    private String profilePic;
} 