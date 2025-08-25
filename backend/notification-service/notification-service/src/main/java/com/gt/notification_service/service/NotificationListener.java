package com.gt.notification_service.service;

import com.gt.notification_service.dto.PassApprovedEvent;
import com.gt.notification_service.dto.PassExpiredEvent;
import com.gt.notification_service.dto.PassRejectedEvent;
import com.gt.notification_service.dto.PasswordResetEvent;
import com.gt.notification_service.dto.UserCreatedEvent;
import com.gt.notification_service.model.EmailAuditLog;
import com.gt.notification_service.model.EmailStatus;
import com.gt.notification_service.repository.EmailAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class NotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);
    // Formatter for a more friendly date and time display in the email
    private static final DateTimeFormatter FRIENDLY_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");

    private final EmailAuditLogRepository emailAuditLogRepository;
    private final EmailSenderService emailSenderService;

    public NotificationListener(EmailAuditLogRepository emailAuditLogRepository, EmailSenderService emailSenderService) {
        this.emailAuditLogRepository = emailAuditLogRepository;
        this.emailSenderService = emailSenderService;
    }

    @RabbitListener(queues = "pass.approved.queue", errorHandler = "rabbitMQErrorHandler")
    public void handlePassApproved(PassApprovedEvent event) {
        logger.info("Received PassApprovedEvent for pass ID: {}. Notifying employee and visitor.", event.getPassId());

        // --- Notification for the Employee (no change here) ---
        String employeeSubject = "Your Visitor Pass Request has been Approved!";
        String employeeBody = String.format("Hello,\n\nThe visitor pass for %s has been approved.\n\nThank you.", event.getVisitorName());
        processEmailNotification(event.getPassId(), event.getEmployeeEmail(), employeeSubject, employeeBody);

        // --- VVV THIS IS THE FIX: Detailed Notification for the Visitor VVV ---
        if (event.getVisitorEmail() != null && !event.getVisitorEmail().isEmpty()) {
            String visitorSubject = "Your Visitor Pass is Confirmed!";
            String visitorBody = String.format(
                "Dear %s,\n\n" +
                "Your visitor pass for your upcoming visit has been confirmed. Please find the details below:\n\n" +
                "============================================\n" +
                "   PASS CODE: %s\n" + // <-- Pass Code added
                "   Date & Time: %s\n" +      // <-- Visit Time added
                "============================================\n\n" +
                "Please be ready to present this pass code to security upon arrival.\n\n" +
                "We look forward to seeing you.",
                event.getVisitorName(),
                event.getPassCode(),
                event.getVisitDateTime().format(FRIENDLY_FORMATTER) // Format for readability
            );
            processEmailNotification(event.getPassId(), event.getVisitorEmail(), visitorSubject, visitorBody);
        } else {
            logger.warn("Visitor email was null or empty for pass ID: {}. Skipping visitor notification.", event.getPassId());
        }
    }

    @RabbitListener(queues = "pass.rejected.queue", errorHandler = "rabbitMQErrorHandler")
    public void handlePassRejected(PassRejectedEvent event) {
        logger.info("Received PassRejectedEvent for pass ID: {}", event.getPassId());
        String subject = "Update on Your Visitor Pass Request";
        String body = String.format(
                "Hello,\n\nUnfortunately, the visitor pass request for %s has been rejected.\n\nReason: %s\n\nThank you.",
                event.getVisitorName(),
                event.getRejectionReason()
        );
        processEmailNotification(event.getPassId(), event.getEmployeeEmail(), subject, body);
    }

    @RabbitListener(queues = "pass.expired.queue", errorHandler = "rabbitMQErrorHandler")
    public void handlePassExpired(PassExpiredEvent event) {
        logger.info("Received PassExpiredEvent for pass ID: {}", event.getPassId());
        String subject = "Visitor Pass Expired: " + event.getVisitorName();
        String body = String.format(
                "This is an automated notification.\n\nThe visitor pass for %s (scheduled for %s) was not used and has been automatically expired by the system.",
                event.getVisitorName(),
                event.getVisitDateTime().toLocalDate().toString()
        );
        processEmailNotification(event.getPassId(), event.getEmployeeEmail(), subject, body);
        if (event.getTenantAdminEmail() != null && !event.getTenantAdminEmail().isEmpty()) {
            processEmailNotification(event.getPassId(), event.getTenantAdminEmail(), subject, body);
        }
    }

    @RabbitListener(queues = "user.created.queue", errorHandler = "rabbitMQErrorHandler")
    public void handleUserCreated(UserCreatedEvent event) {
        logger.info("Received UserCreatedEvent for new user: {}", event.getNewUserEmail());
        String subject = "Welcome to the Visitor Pass Management System!";
        String body = String.format(
                "Hello %s,\n\nAn account has been created for you...\n", // Truncated for brevity
                event.getNewUserName()
        );
        processEmailNotification(null, event.getNewUserEmail(), subject, body);
    }

   // Inside NotificationListener.java

@RabbitListener(queues = "password.reset.queue", errorHandler = "rabbitMQErrorHandler")
public void handlePasswordReset(PasswordResetEvent event) {
    logger.info("Received PasswordResetEvent for user: {}", event.getUserEmail());
    String subject = "Your Password Reset Request";

    // VVV --- THIS IS THE UPGRADED HTML TEMPLATE LOGIC --- VVV
    
    // 1. Create a visually appealing HTML button for the main action.
    String actionButton = "<a href='" + event.getResetUrl() + "' " +
                          "style='background-color: #0d6efd; color: white; padding: 12px 25px; " +
                          "text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>" +
                          "Reset Your Password</a>";

    // 2. Build the different parts of the email content.
    String intro = "We received a request to reset the password for your account associated with the email: " +
                   "<strong>" + event.getUserEmail() + "</strong>. " +
                   "Please click the button below to set a new password.";
    
    // This section will contain the button and a security disclaimer.
    String detailsSection = "<div style='text-align: center; margin: 30px 0;'>" + actionButton + "</div>" +
                            "<p style='font-size: 12px; color: #888; text-align: center;'>" +
                            "This link is valid for 15 minutes for security reasons. If you did not request a password reset, " +
                            "please ignore this email. Your account remains secure.</p>";

    // 3. Call our reusable template builder.
    String body = createHtmlEmailTemplate(
        "Password Reset Request",
        "Hello " + event.getUserName() + ",",
        intro, 
        // We are not using the key-value table for this email, so we pass an empty map.
        java.util.Collections.emptyMap(), 
        detailsSection
    );

    processEmailNotification(null, event.getUserEmail(), subject, body);
}

    private void processEmailNotification(Long passId, String recipientAddress, String subject, String body) {
        EmailAuditLog auditLog = new EmailAuditLog();
        auditLog.setCorrelationId(UUID.randomUUID().toString());
        auditLog.setAssociatedPassId(passId);
        auditLog.setRecipientAddress(recipientAddress);
        auditLog.setSubject(subject);
        auditLog.setBody(body);
        auditLog.setStatus(EmailStatus.PENDING);
        auditLog.setCreatedAt(LocalDateTime.now());
        EmailAuditLog savedLog = emailAuditLogRepository.save(auditLog);

        try {
            boolean wasSent = emailSenderService.sendEmail(recipientAddress, subject, body);
            if (wasSent) {
                savedLog.setStatus(EmailStatus.SENT);
            } else {
                savedLog.setStatus(EmailStatus.FAILED);
                savedLog.setFailureReason("Email provider (SMTP) failed to send the message.");
            }
        } catch (Exception e) {
            logger.error("An exception occurred while sending email for pass ID: {}. Error: {}", passId, e.getMessage());
            savedLog.setStatus(EmailStatus.FAILED);
            savedLog.setFailureReason(e.getMessage());
        }

        savedLog.setProcessedAt(LocalDateTime.now());
        emailAuditLogRepository.save(savedLog);
    }

    private String createHtmlEmailTemplate(String title, String heading, String intro, Map<String, String> details, String outro) {
    StringBuilder detailsHtml = new StringBuilder();
    if (details != null && !details.isEmpty()) {
        details.forEach((key, value) -> {
            detailsHtml.append("<tr><td style='padding: 8px; border-bottom: 1px solid #ddd; background-color: #f9f9f9;'><strong>")
                       .append(key)
                       .append(":</strong></td><td style='padding: 8px; border-bottom: 1px solid #ddd;'>")
                       .append(value)
                       .append("</td></tr>");
        });
    }

    String detailsTable = "";
    if (detailsHtml.length() > 0) {
        detailsTable = "<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>" + detailsHtml.toString() + "</table>";
    }

    return "<!DOCTYPE html>" +
           "<html><head><style>" +
           "body{font-family: Arial, sans-serif; color: #333;}" +
           ".container{max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);}" +
           ".header{background-color: #003366; color: white; padding: 10px 20px; text-align: center; border-radius: 8px 8px 0 0;}" +
           ".content{padding: 20px;}" +
           ".footer{text-align: center; font-size: 12px; color: #888; margin-top: 20px;}" +
           "</style></head><body>" +
           "<div class='container'>" +
           "<div class='header'><h2>" + title + "</h2></div>" +
           "<div class='content'>" +
           "<p><b>" + heading + "</b></p>" +
           "<p>" + intro + "</p>" +
           detailsTable + // <-- Use the generated table
           "<p>" + outro + "</p>" +
           "</div>" +
           "<div class='footer'><p>&copy; " + java.time.Year.now().getValue() + " Visitor Pass System</p></div>" +
           "</div></body></html>";
}
}