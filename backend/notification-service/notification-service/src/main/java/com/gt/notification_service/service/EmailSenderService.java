package com.gt.notification_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
     * Sends a simple text email using SMTP.
     *
     * @param to The recipient's email address.
     * @param subject The subject of the email.
     * @param body The plain text content of the email.
     * @return true if the email was sent successfully, false otherwise.
     */
    public boolean sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            javaMailSender.send(message);
            logger.info("Successfully sent email to {}", to);
            return true;
        } catch (MailException e) {
            logger.error("Failed to send email to {}. Error: {}", to, e.getMessage());
            return false;
        }
    }
}