# OTP & Email Implementation Guide for Margdarshak

This guide provides a complete step-by-step implementation of OTP (One-Time Password) functionality with email delivery for the Margdarshak URL Shortener Service.

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [SMTP Configuration Options](#smtp-configuration-options)
- [Implementation Steps](#implementation-steps)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Security Best Practices](#security-best-practices)

---

## Overview

This implementation adds the following features:
- Generate 6-digit OTP codes
- Send OTP via email (SMTP)
- Verify OTP with expiration (10 minutes)
- Rate limiting (1 OTP per minute per user)
- Support multiple purposes (EMAIL_VERIFICATION, PASSWORD_RESET)
- Single-use OTP (marked as used after verification)

---

## Architecture

```
User Request → Controller → Service Layer → Email Service → SMTP Server
                              ↓
                         OTP Repository
                              ↓
                         Database (otp_master table)
```

**Components:**
1. **OtpEntity** - Database model for storing OTP records
2. **OtpRepository** - Data access layer
3. **EmailService** - Email sending logic
4. **OtpService** - Business logic for OTP generation/verification
5. **OtpController** - REST API endpoints

---

## Prerequisites

- Java 21
- Spring Boot 3.2.3
- PostgreSQL database
- Maven
- Email service account (Gmail/SendGrid/Brevo)

---

## SMTP Configuration Options

### Option 1: Gmail (Easiest - Recommended for Development)

**Best for:** Quick setup, development, testing  
**Free tier:** 500 emails/day  
**Setup time:** 5 minutes

#### Setup Steps:

1. **Enable 2-Factor Authentication**
   ```
   1. Go to: https://myaccount.google.com/security
   2. Click "2-Step Verification"
   3. Follow the prompts to enable it
   ```

2. **Generate App Password**
   ```
   1. Go to: https://myaccount.google.com/apppasswords
   2. Select "Mail" as the app
   3. Select "Other (Custom name)" as device
   4. Name it "Margdarshak App"
   5. Click "Generate"
   6. Copy the 16-character password (format: xxxx xxxx xxxx xxxx)
   ```

3. **Configuration**
   ```properties
   # Gmail SMTP Configuration
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=youremail@gmail.com
   spring.mail.password=your-16-char-app-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   spring.mail.properties.mail.smtp.starttls.required=true
   spring.mail.properties.mail.smtp.connectiontimeout=5000
   spring.mail.properties.mail.smtp.timeout=5000
   spring.mail.properties.mail.smtp.writetimeout=5000
   ```

**Pros:**
- ✅ Very easy setup
- ✅ Free
- ✅ Reliable delivery
- ✅ Perfect for development

**Cons:**
- ❌ 500 emails/day limit
- ❌ Shows personal Gmail address
- ❌ Not professional for production

---

### Option 2: SendGrid (Recommended for Production)

**Best for:** Production environment  
**Free tier:** 100 emails/day (40,000/month with credit card)  
**Setup time:** 15 minutes

#### Setup Steps:

1. **Sign Up**
   ```
   1. Go to: https://sendgrid.com/
   2. Click "Start for Free"
   3. Sign up and verify your email
   ```

2. **Create API Key**
   ```
   1. Login to SendGrid Dashboard
   2. Go to: Settings → API Keys
   3. Click "Create API Key"
   4. Name: "Margdarshak"
   5. Permission Level: "Full Access" or "Restricted Access" with Mail Send
   6. Click "Create & View"
   7. Copy the API key (starts with SG.)
   ```

3. **Verify Sender Identity**

   **Option A: Single Sender Verification (Easier)**
   ```
   1. Go to: Settings → Sender Authentication
   2. Click "Verify a Single Sender"
   3. Fill in:
      - From Name: Margdarshak
      - From Email Address: noreply@yourdomain.com (or any email)
      - Reply To: support@yourdomain.com
      - Company Address, City, Country
   4. Click "Create"
   5. Check your email and verify
   ```

   **Option B: Domain Authentication (More Professional)**
   ```
   1. Go to: Settings → Sender Authentication
   2. Click "Authenticate Your Domain"
   3. Enter your domain (e.g., yourdomain.com)
   4. Follow DNS record instructions
   5. Add CNAME records to your DNS provider
   6. Verify
   ```

4. **Configuration**
   ```properties
   # SendGrid SMTP Configuration
   spring.mail.host=smtp.sendgrid.net
   spring.mail.port=587
   spring.mail.username=apikey
   spring.mail.password=SG.your-actual-api-key-here
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   spring.mail.properties.mail.smtp.starttls.required=true
   ```

**Pros:**
- ✅ Professional service
- ✅ Better deliverability
- ✅ Analytics dashboard
- ✅ Scalable
- ✅ Custom sender addresses

**Cons:**
- ❌ Requires account setup
- ❌ Need to verify sender

---

### Option 3: Brevo (formerly Sendinblue)

**Best for:** Production with higher volume  
**Free tier:** 300 emails/day  
**Setup time:** 10 minutes

#### Setup Steps:

1. **Sign Up**
   ```
   1. Go to: https://www.brevo.com/
   2. Click "Sign up free"
   3. Create account and verify email
   ```

2. **Get SMTP Credentials**
   ```
   1. Login to Brevo Dashboard
   2. Go to: Settings → SMTP & API
   3. Find SMTP settings section
   4. Note your login email
   5. Click "Generate a new SMTP key"
   6. Copy the SMTP key
   ```

3. **Verify Sender**
   ```
   1. Go to: Senders & IP
   2. Click "Add a sender"
   3. Enter sender email and name
   4. Verify the email address
   ```

4. **Configuration**
   ```properties
   # Brevo SMTP Configuration
   spring.mail.host=smtp-relay.brevo.com
   spring.mail.port=587
   spring.mail.username=your-brevo-email@example.com
   spring.mail.password=your-smtp-key
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   spring.mail.properties.mail.smtp.starttls.required=true
   ```

**Pros:**
- ✅ 300 emails/day (3x more than SendGrid)
- ✅ Easy setup
- ✅ Good deliverability
- ✅ Nice dashboard

**Cons:**
- ❌ Less popular than SendGrid
- ❌ Fewer integrations

---

### Option 4: Mailtrap (Development/Testing Only)

**Best for:** Development and testing (emails don't actually send)  
**Free tier:** Unlimited test emails  
**Setup time:** 5 minutes

#### Setup Steps:

1. **Sign Up**
   ```
   1. Go to: https://mailtrap.io/
   2. Sign up for free account
   3. Verify email
   ```

2. **Get Credentials**
   ```
   1. Login to Mailtrap
   2. Go to: Email Testing → Inboxes
   3. Click on "My Inbox" (or create new)
   4. Copy SMTP credentials shown
   ```

3. **Configuration**
   ```properties
   # Mailtrap Configuration (DEVELOPMENT ONLY)
   spring.mail.host=sandbox.smtp.mailtrap.io
   spring.mail.port=2525
   spring.mail.username=your-mailtrap-username
   spring.mail.password=your-mailtrap-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

**Pros:**
- ✅ Perfect for testing
- ✅ Captures all emails
- ✅ Inspect HTML/content
- ✅ No spam risk

**Cons:**
- ❌ Emails don't actually deliver (test only)

---

## Implementation Steps

### Step 1: Add Email Dependency

**File:** `pom.xml`

Add this dependency in the `<dependencies>` section:

```xml
<!-- Spring Boot Mail Starter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

After adding, run:
```bash
mvn clean install
```

---

### Step 2: Create OTP Entity

**File:** `src/main/java/com/ajkumarray/margdarshak/entity/OtpEntity.java`

```java
package com.ajkumarray.margdarshak.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_master")
@Getter
@Setter
public class OtpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "otp", nullable = false)
    private String otp;

    @Column(name = "purpose", nullable = false)
    private String purpose; // EMAIL_VERIFICATION, PASSWORD_RESET, etc.

    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
}
```

**Database Migration (SQL):**
```sql
CREATE TABLE otp_master (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP
);

CREATE INDEX idx_otp_email ON otp_master(email);
CREATE INDEX idx_otp_purpose ON otp_master(purpose);
CREATE INDEX idx_otp_expires_at ON otp_master(expires_at);
```

---

### Step 3: Create OTP Repository

**File:** `src/main/java/com/ajkumarray/margdarshak/repository/OtpRepository.java`

```java
package com.ajkumarray.margdarshak.repository;

import com.ajkumarray.margdarshak.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, Long> {
    
    Optional<OtpEntity> findByEmailAndOtpAndPurposeAndIsUsedFalseAndExpiresAtAfter(
        String email, 
        String otp, 
        String purpose, 
        LocalDateTime currentTime
    );
    
    Optional<OtpEntity> findTopByEmailAndPurposeOrderByCreatedAtDesc(
        String email, 
        String purpose
    );
}
```

---

### Step 4: Create Email Service

**File:** `src/main/java/com/ajkumarray/margdarshak/service/EmailService.java`

```java
package com.ajkumarray.margdarshak.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verify Your Email - Margdarshak");
            message.setText(buildOtpEmailContent(userName, otp));
            
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    private String buildOtpEmailContent(String userName, String otp) {
        return String.format(
            "Hello %s,\n\n" +
            "Your OTP for email verification is: %s\n\n" +
            "This OTP will expire in 10 minutes.\n\n" +
            "If you didn't request this verification, please ignore this email.\n\n" +
            "Best regards,\n" +
            "Margdarshak Team",
            userName, otp
        );
    }

    public void sendPasswordResetOtp(String toEmail, String otp, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset OTP - Margdarshak");
            message.setText(buildPasswordResetEmailContent(userName, otp));
            
            mailSender.send(message);
            log.info("Password reset OTP email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send password reset OTP email");
        }
    }

    private String buildPasswordResetEmailContent(String userName, String otp) {
        return String.format(
            "Hello %s,\n\n" +
            "Your OTP for password reset is: %s\n\n" +
            "This OTP will expire in 10 minutes.\n\n" +
            "If you didn't request a password reset, please ignore this email and secure your account.\n\n" +
            "Best regards,\n" +
            "Margdarshak Team",
            userName, otp
        );
    }
}
```

---

### Step 5: Create OTP Service

**File:** `src/main/java/com/ajkumarray/margdarshak/service/OtpService.java`

```java
package com.ajkumarray.margdarshak.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ajkumarray.margdarshak.entity.OtpEntity;
import com.ajkumarray.margdarshak.entity.UserMasterEntity;
import com.ajkumarray.margdarshak.enums.ApplicationEnums;
import com.ajkumarray.margdarshak.exception.ApplicationException;
import com.ajkumarray.margdarshak.repository.OtpRepository;
import com.ajkumarray.margdarshak.repository.UserRepository;
import com.ajkumarray.margdarshak.util.MessageTranslator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;

    /**
     * Generate and send OTP to user's email
     */
    @Transactional
    public void generateAndSendOtp(String email, String purpose) {
        // Check if user exists
        Optional<UserMasterEntity> userEntity = userRepository.findByEmail(email);
        if (userEntity.isEmpty()) {
            throw new ApplicationException(
                MessageTranslator.toLocale(ApplicationEnums.INVALID_USER_CODE.getCode()),
                ApplicationEnums.INVALID_USER_CODE.getCode()
            );
        }

        UserMasterEntity user = userEntity.get();

        // Check if OTP was recently sent (rate limiting)
        Optional<OtpEntity> recentOtp = otpRepository
            .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose);
        
        if (recentOtp.isPresent()) {
            LocalDateTime lastOtpTime = recentOtp.get().getCreatedAt();
            if (lastOtpTime.plusMinutes(1).isAfter(LocalDateTime.now())) {
                throw new ApplicationException(
                    "Please wait before requesting another OTP",
                    "OTP_RATE_LIMIT"
                );
            }
        }

        // Generate OTP
        String otp = generateOtp();

        // Save OTP to database
        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setPurpose(purpose);
        otpEntity.setCreatedAt(LocalDateTime.now());
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otpEntity.setUsed(false);
        
        otpRepository.save(otpEntity);

        // Send OTP via email
        if ("EMAIL_VERIFICATION".equals(purpose)) {
            emailService.sendOtpEmail(email, otp, user.getName());
        } else if ("PASSWORD_RESET".equals(purpose)) {
            emailService.sendPasswordResetOtp(email, otp, user.getName());
        }

        log.info("OTP generated and sent successfully for email: {} with purpose: {}", email, purpose);
    }

    /**
     * Verify OTP
     */
    @Transactional
    public boolean verifyOtp(String email, String otp, String purpose) {
        Optional<OtpEntity> otpEntity = otpRepository
            .findByEmailAndOtpAndPurposeAndIsUsedFalseAndExpiresAtAfter(
                email, otp, purpose, LocalDateTime.now()
            );

        if (otpEntity.isPresent()) {
            OtpEntity otpRecord = otpEntity.get();
            otpRecord.setUsed(true);
            otpRecord.setVerifiedAt(LocalDateTime.now());
            otpRepository.save(otpRecord);
            
            log.info("OTP verified successfully for email: {}", email);
            return true;
        }

        log.warn("Invalid or expired OTP for email: {}", email);
        return false;
    }

    /**
     * Generate random numeric OTP
     */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
}
```

---

### Step 6: Create Request DTOs

**File:** `src/main/java/com/ajkumarray/margdarshak/models/request/SendOtpRequest.java`

```java
package com.ajkumarray.margdarshak.models.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendOtpRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Purpose is required")
    private String purpose; // EMAIL_VERIFICATION, PASSWORD_RESET
}
```

**File:** `src/main/java/com/ajkumarray/margdarshak/models/request/VerifyOtpRequest.java`

```java
package com.ajkumarray.margdarshak.models.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;
    
    @NotBlank(message = "Purpose is required")
    private String purpose;
}
```

---

### Step 7: Create Response DTO

**File:** `src/main/java/com/ajkumarray/margdarshak/models/response/OtpResponse.java`

```java
package com.ajkumarray.margdarshak.models.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OtpResponse extends ParentResponse {
    private String message;
    private boolean success;
}
```

---

### Step 8: Create OTP Controller

**File:** `src/main/java/com/ajkumarray/margdarshak/controller/OtpController.java`

```java
package com.ajkumarray.margdarshak.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ajkumarray.margdarshak.models.request.SendOtpRequest;
import com.ajkumarray.margdarshak.models.request.VerifyOtpRequest;
import com.ajkumarray.margdarshak.models.response.OtpResponse;
import com.ajkumarray.margdarshak.service.OtpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("${spring.api}otp")
@Tag(name = "OTP Management", description = "APIs for OTP generation and verification")
@Slf4j
public class OtpController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/send")
    @Operation(summary = "Send OTP", description = "Generate and send OTP to user's email")
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        log.info("Sending OTP to email: {} for purpose: {}", request.getEmail(), request.getPurpose());
        
        otpService.generateAndSendOtp(request.getEmail(), request.getPurpose());
        
        OtpResponse response = new OtpResponse();
        response.setSuccess(true);
        response.setMessage("OTP sent successfully to your email");
        response.setMessageCode("OTP_SENT_SUCCESS");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify OTP", description = "Verify the OTP sent to user's email")
    public ResponseEntity<OtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("Verifying OTP for email: {}", request.getEmail());
        
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp(), request.getPurpose());
        
        OtpResponse response = new OtpResponse();
        response.setSuccess(isValid);
        response.setMessage(isValid ? "OTP verified successfully" : "Invalid or expired OTP");
        response.setMessageCode(isValid ? "OTP_VERIFIED_SUCCESS" : "OTP_VERIFICATION_FAILED");
        
        return ResponseEntity.ok(response);
    }
}
```

---

### Step 9: Update Application Properties

**File:** `src/main/resources/application.properties`

Add email configuration (choose one SMTP provider):

```properties
# ===== CHOOSE ONE SMTP CONFIGURATION BELOW =====

# Option 1: Gmail (Development)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=youremail@gmail.com
spring.mail.password=your-16-char-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Option 2: SendGrid (Production)
# spring.mail.host=smtp.sendgrid.net
# spring.mail.port=587
# spring.mail.username=apikey
# spring.mail.password=SG.your-sendgrid-api-key
# spring.mail.properties.mail.smtp.auth=true
# spring.mail.properties.mail.smtp.starttls.enable=true

# Option 3: Brevo (Production)
# spring.mail.host=smtp-relay.brevo.com
# spring.mail.port=587
# spring.mail.username=your-brevo-email@example.com
# spring.mail.password=your-brevo-smtp-key
# spring.mail.properties.mail.smtp.auth=true
# spring.mail.properties.mail.smtp.starttls.enable=true

# Option 4: Mailtrap (Testing Only)
# spring.mail.host=sandbox.smtp.mailtrap.io
# spring.mail.port=2525
# spring.mail.username=your-mailtrap-username
# spring.mail.password=your-mailtrap-password
# spring.mail.properties.mail.smtp.auth=true
# spring.mail.properties.mail.smtp.starttls.enable=true

# Common mail properties
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000
```

---

### Step 10: Update Security Configuration

**File:** `src/main/java/com/ajkumarray/margdarshak/config/SecurityConfig.java`

Add OTP endpoints to permitted URLs:

```java
// In the permitAll() section, add:
.requestMatchers("/api/v1/otp/**").permitAll()
```

Example:
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers("/api/v1/public/**").permitAll()
            .requestMatchers("/api/v1/otp/**").permitAll()  // Add this line
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated()
        );
    // ... rest of configuration
}
```

---

### Step 11: Update Error Codes (Optional but Recommended)

**File:** `src/main/java/com/ajkumarray/margdarshak/errors/ResponseCodeAndMessage.java`

Add these constants:

```java
public static final String OTP_SENT_SUCCESS = "020";
public static final String OTP_SEND_FAILED = "021";
public static final String OTP_VERIFIED_SUCCESS = "022";
public static final String OTP_VERIFICATION_FAILED = "023";
public static final String OTP_RATE_LIMIT = "024";
public static final String OTP_EXPIRED = "025";
```

**File:** `src/main/java/com/ajkumarray/margdarshak/enums/ApplicationEnums.java`

Add enum values:

```java
OTP_SENT_SUCCESS(ResponseCodeAndMessage.OTP_SENT_SUCCESS),
OTP_SEND_FAILED(ResponseCodeAndMessage.OTP_SEND_FAILED),
OTP_VERIFIED_SUCCESS(ResponseCodeAndMessage.OTP_VERIFIED_SUCCESS),
OTP_VERIFICATION_FAILED(ResponseCodeAndMessage.OTP_VERIFICATION_FAILED),
OTP_RATE_LIMIT(ResponseCodeAndMessage.OTP_RATE_LIMIT),
OTP_EXPIRED(ResponseCodeAndMessage.OTP_EXPIRED);
```

---

## API Endpoints

### 1. Send OTP

**Endpoint:** `POST /api/v1/otp/send`

**Request:**
```json
{
  "email": "user@example.com",
  "purpose": "EMAIL_VERIFICATION"
}
```

**Purposes:**
- `EMAIL_VERIFICATION` - For verifying user email
- `PASSWORD_RESET` - For password reset flow

**Response (Success):**
```json
{
  "success": true,
  "message": "OTP sent successfully to your email",
  "messageCode": "OTP_SENT_SUCCESS"
}
```

**Response (Error - User Not Found):**
```json
{
  "success": false,
  "message": "Invalid user",
  "messageCode": "M01018"
}
```

**Response (Error - Rate Limit):**
```json
{
  "success": false,
  "message": "Please wait before requesting another OTP",
  "messageCode": "OTP_RATE_LIMIT"
}
```

---

### 2. Verify OTP

**Endpoint:** `POST /api/v1/otp/verify`

**Request:**
```json
{
  "email": "user@example.com",
  "otp": "123456",
  "purpose": "EMAIL_VERIFICATION"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "OTP verified successfully",
  "messageCode": "OTP_VERIFIED_SUCCESS"
}
```

**Response (Error - Invalid OTP):**
```json
{
  "success": false,
  "message": "Invalid or expired OTP",
  "messageCode": "OTP_VERIFICATION_FAILED"
}
```

---

## Testing

### Manual Testing with cURL

**1. Send OTP:**
```bash
curl -X POST http://localhost:8080/api/v1/otp/send \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "purpose": "EMAIL_VERIFICATION"
  }'
```

**2. Verify OTP:**
```bash
curl -X POST http://localhost:8080/api/v1/otp/verify \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "otp": "123456",
    "purpose": "EMAIL_VERIFICATION"
  }'
```

### Testing with Postman

**Collection Setup:**

1. **Send OTP Request**
   - Method: POST
   - URL: `http://localhost:8080/api/v1/otp/send`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "email": "test@example.com",
       "purpose": "EMAIL_VERIFICATION"
     }
     ```

2. **Verify OTP Request**
   - Method: POST
   - URL: `http://localhost:8080/api/v1/otp/verify`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "email": "test@example.com",
       "otp": "123456",
       "purpose": "EMAIL_VERIFICATION"
     }
     ```

### Swagger UI Testing

1. Start your application
2. Go to: `http://localhost:8080/swagger-ui.html`
3. Find "OTP Management" section
4. Test both endpoints directly from Swagger

---

## Troubleshooting

### Issue 1: "Username and Password not accepted"

**Cause:** Using Gmail password instead of App Password

**Solution:**
1. Enable 2-Factor Authentication on Gmail
2. Generate App Password at https://myaccount.google.com/apppasswords
3. Use the 16-character App Password (without spaces)

---

### Issue 2: "Could not connect to SMTP host"

**Causes:**
- Firewall blocking port 587
- Incorrect SMTP host
- Network issues

**Solutions:**
1. Check firewall settings
2. Try alternative port 465 with SSL:
   ```properties
   spring.mail.port=465
   spring.mail.properties.mail.smtp.ssl.enable=true
   ```
3. Verify SMTP host is correct

---

### Issue 3: Emails going to spam

**Causes:**
- Using personal Gmail for mass emails
- No SPF/DKIM records
- High sending frequency

**Solutions:**
1. Use professional email service (SendGrid/Brevo)
2. Verify domain authentication
3. Add SPF/DKIM records to DNS
4. Implement rate limiting
5. Use verified sender addresses

---

### Issue 4: "Authentication failed"

**Causes:**
- Incorrect credentials
- Extra spaces in password
- Wrong username format

**Solutions:**
1. Double-check credentials
2. For SendGrid: username must be exactly `apikey`
3. Remove any spaces from password
4. Verify email format

---

### Issue 5: Table doesn't exist

**Cause:** Database table not created

**Solution:**
Run the SQL migration:
```sql
CREATE TABLE otp_master (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP
);

CREATE INDEX idx_otp_email ON otp_master(email);
CREATE INDEX idx_otp_purpose ON otp_master(purpose);
CREATE INDEX idx_otp_expires_at ON otp_master(expires_at);
```

Or let Hibernate auto-create with:
```properties
spring.jpa.hibernate.ddl-auto=update
```

---

### Issue 6: OTP not expiring

**Cause:** System time mismatch or timezone issues

**Solution:**
1. Verify system time is correct
2. Add timezone configuration:
   ```properties
   spring.jpa.properties.hibernate.jdbc.time_zone=UTC
   ```

---

## Security Best Practices

### 1. Never Commit Credentials

**Bad:**
```properties
# application.properties
spring.mail.password=mypassword123
```

**Good:**
```properties
# application.properties
spring.mail.password=${MAIL_PASSWORD}
```

Then use environment variables:
```bash
export MAIL_PASSWORD=your-actual-password
```

---

### 2. Use Environment-Specific Configuration

**Create:** `application-dev.properties`
```properties
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.username=dev-username
spring.mail.password=dev-password
```

**Create:** `application-prod.properties`
```properties
spring.mail.host=smtp.sendgrid.net
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
```

**Run with profile:**
```bash
# Development
mvn spring-boot:run -Dspring.profiles.active=dev

# Production
mvn spring-boot:run -Dspring.profiles.active=prod
```

---

### 3. Add Rate Limiting

Already implemented in `OtpService.java`:
- 1 OTP per minute per user
- Prevents abuse

**Enhance with IP-based limiting (future):**
```java
// Track OTP requests by IP address
// Limit 5 requests per IP per hour
```

---

### 4. Implement OTP Attempt Limits

```java
// Add to OtpService.java
private static final int MAX_ATTEMPTS = 3;

public boolean verifyOtp(String email, String otp, String purpose) {
    // Check attempt count
    long attempts = otpRepository.countFailedAttempts(email, purpose);
    if (attempts >= MAX_ATTEMPTS) {
        throw new ApplicationException("Too many failed attempts", "OTP_MAX_ATTEMPTS");
    }
    
    // Verify logic...
}
```

---

### 5. Add Gitignore for Sensitive Files

**File:** `.gitignore`

Add:
```
# Application properties with credentials
application-local.properties
application-prod.properties

# Environment files
.env
.env.local
```

---

### 6. Use SSL/TLS

Always use secure connection:
```properties
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

For port 465:
```properties
spring.mail.properties.mail.smtp.ssl.enable=true
```

---

### 7. Clean Up Expired OTPs

**Create scheduled task:**

```java
@Scheduled(cron = "0 0 2 * * *") // Run at 2 AM daily
public void cleanupExpiredOtps() {
    LocalDateTime threshold = LocalDateTime.now().minusDays(7);
    otpRepository.deleteByCreatedAtBefore(threshold);
    log.info("Cleaned up expired OTPs");
}
```

---

## Production Checklist

Before deploying to production:

- [ ] Choose production SMTP provider (SendGrid/Brevo)
- [ ] Set up environment variables for credentials
- [ ] Verify domain authentication (SPF/DKIM)
- [ ] Test email deliverability
- [ ] Enable logging for debugging
- [ ] Set up monitoring/alerts
- [ ] Configure rate limiting
- [ ] Add OTP cleanup job
- [ ] Test error scenarios
- [ ] Document API for frontend team
- [ ] Set up SSL/TLS certificates
- [ ] Configure CORS properly
- [ ] Add request validation
- [ ] Implement retry mechanism
- [ ] Set up backup SMTP provider

---

## Integration with User Verification Flow

### Complete User Registration + Verification Flow

**1. User Registration:**
```java
@PostMapping("/register")
public AuthResponse register(@RequestBody UserMasterRequest request) {
    // Create user with UNVERIFIED status
    UserMasterEntity user = userService.register(request);
    user.setVerificationStatus(UserVerificationStatusEnums.UNVERIFIED);
    
    // Send OTP automatically
    otpService.generateAndSendOtp(user.getEmail(), "EMAIL_VERIFICATION");
    
    return authResponse;
}
```

**2. User Verifies Email:**
```java
@PostMapping("/verify-email")
public Response verifyEmail(@RequestBody VerifyOtpRequest request) {
    boolean isValid = otpService.verifyOtp(
        request.getEmail(), 
        request.getOtp(), 
        "EMAIL_VERIFICATION"
    );
    
    if (isValid) {
        // Update user status
        UserMasterEntity user = userRepository.findByEmail(request.getEmail());
        user.setVerificationStatus(UserVerificationStatusEnums.VERIFIED);
        userRepository.save(user);
    }
    
    return response;
}
```

---

## FAQ

**Q: How long is the OTP valid?**  
A: 10 minutes by default (configurable in `OtpService.java`)

**Q: Can I use the same OTP multiple times?**  
A: No, OTP is single-use and marked as used after verification

**Q: How many OTPs can I send per day?**  
A: Depends on SMTP provider:
- Gmail: 500/day
- SendGrid: 100/day (free tier)
- Brevo: 300/day (free tier)

**Q: What happens if OTP expires?**  
A: User must request a new OTP

**Q: Can I change OTP length?**  
A: Yes, modify `OTP_LENGTH` constant in `OtpService.java`

**Q: How do I test without sending real emails?**  
A: Use Mailtrap for testing

**Q: Is it secure?**  
A: Yes, uses:
- SecureRandom for OTP generation
- TLS/SSL for email transmission
- Database storage with expiration
- Rate limiting

---

## Support

For issues or questions:
- Check logs in console
- Review Swagger documentation at `/swagger-ui.html`
- Check SMTP provider status page
- Verify database connectivity
- Review application.properties configuration

---

## License

This implementation is part of the Margdarshak project.

---

**Last Updated:** October 2025  
**Version:** 1.0.0  
**Author:** Margdarshak Team





