package com.gt.visitor_pass_service.service;

import com.gt.visitor_pass_service.config.security.JwtTokenProvider;
import com.gt.visitor_pass_service.dto.*;
import com.gt.visitor_pass_service.exception.ResourceNotFoundException;
import com.gt.visitor_pass_service.model.PasswordResetToken;
import com.gt.visitor_pass_service.model.User;
import com.gt.visitor_pass_service.repository.PasswordResetTokenRepository;
import com.gt.visitor_pass_service.repository.UserRepository;
import com.gt.visitor_pass_service.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;
    private final DirectEmailService directEmailService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtTokenProvider tokenProvider,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       RabbitTemplate rabbitTemplate,
                       DirectEmailService directEmailService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.rabbitTemplate = rabbitTemplate;
        this.directEmailService = directEmailService;
    }

    /**
     * Authenticates a user based on login credentials and generates a JWT.
     * @param loginRequest DTO containing the username (email) and password.
     * @return A JWT string if authentication is successful.
     */
    public String loginAndGetToken(LoginRequest loginRequest) {
        // Step 1: Use Spring Security's AuthenticationManager to validate the credentials.
        // This will automatically use your CustomUserDetailsService to find the user
        // and BCryptPasswordEncoder to compare the passwords.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Step 2: If authentication is successful, set the authentication object in the security context.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Step 3: Fetch the full User object to include its details (like ID, tenantId) in the token.
        User user = userRepository.findByEmail(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", loginRequest.getUsername()));

        // Step 4: Generate the JWT using the authenticated principal and the full user object.
        return tokenProvider.generateToken(authentication, user);
    }

    /**
     * Initiates the password reset process by generating a reset token and sending an email.
     * @param request DTO containing the user's email address.
     */
    @Transactional
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        logger.info("Initiating password reset for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        logger.info("Found user for password reset: {} (ID: {})", user.getEmail(), user.getId());

        // Invalidate any existing tokens for this user
        passwordResetTokenRepository.markAllUserTokensAsUsed(user);
        logger.info("Invalidated existing tokens for user: {}", user.getEmail());

        // Generate a secure random token
        String token = generateSecureToken();
        logger.info("Generated reset token for user: {} (Token length: {})", user.getEmail(), token.length());

        // Set expiry time to 15 minutes from now
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

        // Create and save the reset token
        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);
        passwordResetTokenRepository.save(resetToken);
        logger.info("Saved reset token to database for user: {}", user.getEmail());

        // Create the reset URL
        String resetUrl = "http://localhost:4200/reset-password/" + token;

        // Create and send the password reset event
        PasswordResetEvent event = new PasswordResetEvent(
                user.getEmail(),
                user.getName(),
                token,
                resetUrl,
                user.getTenant() != null ? user.getTenant().getName() : "Visitor Pass System"
        );

        logger.info("Sending password reset event to RabbitMQ for user: {} with URL: {}", user.getEmail(), resetUrl);
        try {
           rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_PASSWORD_RESET, event);
            logger.info("Password reset event sent successfully to RabbitMQ for user: {}", user.getEmail());
        } catch (Exception e) {
            logger.warn("Failed to send to RabbitMQ, trying direct email for user: {}", user.getEmail(), e);
            // Fallback to direct email
            boolean emailSent = directEmailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getName(),
                    resetUrl,
                    user.getTenant() != null ? user.getTenant().getName() : "Visitor Pass System"
            );
            if (emailSent) {
                logger.info("Password reset email sent directly for user: {}", user.getEmail());
            } else {
                logger.error("Failed to send password reset email for user: {}", user.getEmail());
                throw new RuntimeException("Failed to send password reset email");
            }
        }
    }

    /**
     * Resets the user's password using a valid reset token.
     * @param request DTO containing the reset token and new password.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Validate that passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Find and validate the reset token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (!resetToken.isValid()) {
            throw new IllegalArgumentException("Reset token has expired or has already been used");
        }

        // Update the user's password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark the token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Clean up expired tokens
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    /**
     * Generates a secure random token for password reset.
     * @return A base64-encoded secure random token.
     */
    private String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Test method to send email directly without RabbitMQ
     */
    public void testDirectEmail(String email) {
        logger.info("Testing direct email to: {}", email);

        // Try direct email first
        boolean emailSent = directEmailService.sendPasswordResetEmail(
                email,
                "Test User",
                "http://localhost:4200/reset-password/test-token-123",
                "Test Tenant"
        );

        if (emailSent) {
            logger.info("Test email sent successfully via direct email to: {}", email);
        } else {
            logger.error("Failed to send test email to: {}", email);
            throw new RuntimeException("Failed to send test email");
        }
    }
}