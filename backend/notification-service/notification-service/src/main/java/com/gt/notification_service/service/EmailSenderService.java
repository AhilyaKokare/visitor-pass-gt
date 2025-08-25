package com.gt.notification_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    private static final Logger logger = LoggerFactory.getLogger(EmailSenderService.class);

    private final JavaMailSender javaMailSender;
    private final String fromEmail;

    public EmailSenderService(JavaMailSender javaMailSender,
                              @Value("${spring.mail.username}") String fromEmail) {
        this.javaMailSender = javaMailSender;
        this.fromEmail = fromEmail;
    }

    /**
     * Sends a rich HTML email using SMTP.
     *
     * @param to The recipient's email address.
     * @param subject The subject of the email.
     * @param htmlBody The HTML content of the email.
     * @return true if the email was sent successfully, false otherwise.
     */
    public boolean sendEmail(String to, String subject, String htmlBody) {
        // VVV --- THIS IS THE UPGRADED LOGIC --- VVV
        
        // Create a MimeMessage that can handle HTML
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            // Use the MimeMessageHelper to build the email
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            
            // This is the most critical line: Set the body and specify that it IS HTML.
            helper.setText(htmlBody, true);

            javaMailSender.send(message);
            logger.info("Successfully sent HTML email to {}", to);
            return true;
        
        // Catch both potential exception types for robustness
        } catch (MessagingException | MailException e) {
            logger.error("Failed to send HTML email to {}. Error: {}", to, e.getMessage());
            return false;
        }
    }
}