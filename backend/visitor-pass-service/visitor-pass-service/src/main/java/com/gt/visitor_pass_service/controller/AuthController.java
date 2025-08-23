package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.dto.*;
import com.gt.visitor_pass_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "0. Authentication", description = "API for user login to obtain a JWT.")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // VVV INJECT AuthService INSTEAD OF THE OTHER COMPONENTS VVV
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate User", description = "Authenticates a user with email and password, returning a JWT if successful.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtAuthenticationResponse.class)) }),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    })
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // VVV THE LOGIC IS NOW A SIMPLE, SINGLE CALL TO THE SERVICE VVV
        String jwt = authService.loginAndGetToken(loginRequest);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate Password Reset", description = "Sends a password reset email to the user if the email exists in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found with the provided email", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid email format", content = @Content)
    })
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        logger.info("Received forgot password request for email: {}", request.getEmail());
        try {
            authService.initiatePasswordReset(request);
            logger.info("Password reset initiated successfully for email: {}", request.getEmail());
            return ResponseEntity.ok("Password reset email sent successfully. Please check your email.");
        } catch (Exception e) {
            logger.error("Error during password reset initiation for email: {}", request.getEmail(), e);
            // For security reasons, we don't reveal if the email exists or not
            return ResponseEntity.ok("If the email exists in our system, a password reset link has been sent.");
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password", description = "Resets the user's password using a valid reset token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token, expired token, or password validation failed", content = @Content)
    })
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        logger.info("Received password reset request with token: {}", request.getToken());
        try {
            authService.resetPassword(request);
            logger.info("Password reset completed successfully for token: {}", request.getToken());
            return ResponseEntity.ok("Password reset successfully. You can now login with your new password.");
        } catch (IllegalArgumentException e) {
            logger.warn("Password reset failed for token: {} - {}", request.getToken(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during password reset for token: {}", request.getToken(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while resetting the password. Please try again.");
        }
    }

    @PostMapping("/test-email")
    @Operation(summary = "Test Email Functionality", description = "Test endpoint to verify email sending without RabbitMQ")
    public ResponseEntity<String> testEmail(@RequestBody ForgotPasswordRequest request) {
        logger.info("Testing direct email functionality for: {}", request.getEmail());
        try {
            authService.testDirectEmail(request.getEmail());
            return ResponseEntity.ok("Test email sent successfully!");
        } catch (Exception e) {
            logger.error("Test email failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Test email failed: " + e.getMessage());
        }
    }
}