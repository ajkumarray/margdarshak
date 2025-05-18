package com.ajkumarray.margdarshak.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a user in the system.
 * Maps to the 'users' table in the database.
 * This entity contains all user-related information including authentication details.
 *
 * @author Ajit Kumar
 * @version 1.0
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    /**
     * Unique identifier for the user.
     * Generated using a custom sequence.
     * This is the primary key of the users table.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @Column(name = "user_code", nullable = false, unique = true)
    @NotBlank(message = "User code cannot be empty")
    private String userCode;

    /**
     * User's full name.
     * Cannot be null or empty.
     * Used for display purposes.
     */
    @Column(name = "name", nullable = false)
    @NotBlank(message = "Name cannot be empty")
    private String name;

    /**
     * User's email address.
     * Must be unique and cannot be null.
     * Used for authentication and communication.
     */
    @Column(name = "email", nullable = false, unique = true)
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * User's encrypted password.
     * Cannot be null.
     * Stored in encrypted format using BCrypt.
     */
    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password cannot be empty")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
        message = "Password must be at least 8 characters long and contain at least one digit, one uppercase letter, one lowercase letter, and one special character"
    )
    private String password;

    /**
     * URL to the user's profile picture.
     * Can be null.
     * Used for displaying user's profile image.
     */
    @Column(name = "profile_pic")
    private String profilePic;

    /**
     * Flag indicating if the user's email has been verified.
     * Defaults to false.
     * Used for email verification status tracking.
     */
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    /**
     * Timestamp when the user was created.
     * Automatically set by Hibernate.
     * Cannot be null and cannot be updated.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull(message = "Created at timestamp cannot be null")
    private LocalDateTime createdAt;

    /**
     * Timestamp when the user was last updated.
     * Automatically updated by Hibernate.
     * Cannot be null.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @NotNull(message = "Updated at timestamp cannot be null")
    private LocalDateTime updatedAt;

    /**
     * Flag indicating if the user has been deleted.
     * Defaults to false.
     * Used for soft delete functionality.
     */
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * Timestamp when the user was deleted.
     * Can be null.
     * Used for tracking soft delete operations.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
} 