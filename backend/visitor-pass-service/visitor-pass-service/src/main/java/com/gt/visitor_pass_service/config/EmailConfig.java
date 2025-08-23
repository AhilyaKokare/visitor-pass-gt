package com.gt.visitor_pass_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // If no email configuration is provided, create a mock sender for development
        if (username == null || username.trim().isEmpty()) {
            System.out.println("=== EMAIL CONFIGURATION ===");
            System.out.println("No email credentials configured. Using mock email sender for development.");
            System.out.println("To enable real emails, set MAIL_USERNAME and MAIL_PASSWORD environment variables.");
            
            // Return a mock sender that logs emails instead of sending them
            return new MockJavaMailSender();
        }
        
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        props.put("mail.debug", "false");

        System.out.println("=== EMAIL CONFIGURATION ===");
        System.out.println("Email host: " + host);
        System.out.println("Email port: " + port);
        System.out.println("Email username: " + username);
        System.out.println("Real email sender configured successfully.");

        return mailSender;
    }
}
