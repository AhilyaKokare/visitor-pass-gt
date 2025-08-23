package com.gt.visitor_pass_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class DirectEmailService {

    private static final Logger logger = LoggerFactory.getLogger(DirectEmailService.class);

    private final JavaMailSender javaMailSender;
    private final String fromEmail;

    public DirectEmailService(JavaMailSender javaMailSender,
                              @Value("${spring.mail.username:noreply@visitorpass.com}") String fromEmail) {
        this.javaMailSender = javaMailSender;
        this.fromEmail = fromEmail;
    }

    /**
     * Sends a password reset email directly without RabbitMQ
     */
    public boolean sendPasswordResetEmail(String toEmail, String userName, String resetUrl, String tenantName) {
        try {
            String subject = "Password Reset Request - " + tenantName;
            String body = String.format(
                    "Hello %s,\n\n" +
                    "We received a request to reset your password for your %s account.\n\n" +
                    "To reset your password, please click the link below:\n" +
                    "%s\n\n" +
                    "This link will expire in 15 minutes for security reasons.\n\n" +
                    "If you did not request this password reset, please ignore this email. " +
                    "Your password will remain unchanged.\n\n" +
                    "For security reasons, please do not share this link with anyone.\n\n" +
                    "Best regards,\n" +
                    "The %s Team\n\n" +
                    "---\n" +
                    "This is an automated message. Please do not reply to this email.",
                    userName,
                    tenantName,
                    resetUrl,
                    tenantName
            );

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);
            logger.info("Successfully sent password reset email to {}", toEmail);
            return true;
        } catch (MailException e) {
            logger.error("Failed to send password reset email to {}. Error: {}", toEmail, e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error sending email to {}", toEmail, e);
            return false;
        }
    }
}
