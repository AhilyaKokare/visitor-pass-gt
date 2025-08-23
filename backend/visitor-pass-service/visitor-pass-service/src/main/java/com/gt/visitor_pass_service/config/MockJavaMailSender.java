package com.gt.visitor_pass_service.config;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

/**
 * Mock JavaMailSender for development environment
 * Logs email content instead of actually sending emails
 */
public class MockJavaMailSender implements JavaMailSender {

    @Override
    public MimeMessage createMimeMessage() {
        return new MimeMessage(Session.getDefaultInstance(new Properties()));
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        return createMimeMessage();
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        try {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("📧 MOCK EMAIL SENDER - EMAIL WOULD BE SENT");
            System.out.println("=".repeat(80));
            System.out.println("From: " + (mimeMessage.getFrom() != null && mimeMessage.getFrom().length > 0 ? 
                              mimeMessage.getFrom()[0].toString() : "Not specified"));
            System.out.println("To: " + (mimeMessage.getAllRecipients() != null && mimeMessage.getAllRecipients().length > 0 ? 
                            mimeMessage.getAllRecipients()[0].toString() : "Not specified"));
            System.out.println("Subject: " + mimeMessage.getSubject());
            System.out.println("Content Type: " + mimeMessage.getContentType());
            System.out.println("Content: " + mimeMessage.getContent().toString());
            System.out.println("=".repeat(80));
            System.out.println("✅ Email logged successfully (not actually sent)");
            System.out.println("💡 To send real emails, configure MAIL_USERNAME and MAIL_PASSWORD");
            System.out.println("=".repeat(80) + "\n");
        } catch (Exception e) {
            System.err.println("Error logging mock email: " + e.getMessage());
        }
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        for (MimeMessage message : mimeMessages) {
            send(message);
        }
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        try {
            MimeMessage mimeMessage = createMimeMessage();
            mimeMessagePreparator.prepare(mimeMessage);
            send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Error preparing mock email: " + e.getMessage());
        }
    }

    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
        for (MimeMessagePreparator preparator : mimeMessagePreparators) {
            send(preparator);
        }
    }

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📧 MOCK EMAIL SENDER - SIMPLE EMAIL WOULD BE SENT");
        System.out.println("=".repeat(80));
        System.out.println("From: " + simpleMessage.getFrom());
        System.out.println("To: " + String.join(", ", simpleMessage.getTo() != null ? simpleMessage.getTo() : new String[]{"Not specified"}));
        System.out.println("Subject: " + simpleMessage.getSubject());
        System.out.println("Text: " + simpleMessage.getText());
        System.out.println("=".repeat(80));
        System.out.println("✅ Simple email logged successfully (not actually sent)");
        System.out.println("💡 To send real emails, configure MAIL_USERNAME and MAIL_PASSWORD");
        System.out.println("=".repeat(80) + "\n");
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        for (SimpleMailMessage message : simpleMessages) {
            send(message);
        }
    }
}
