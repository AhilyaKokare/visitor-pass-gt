package com.gt.notification_service.service;

import com.gt.notification_service.dto.PassApprovedEvent;
import com.gt.notification_service.dto.PassExpiredEvent;
import com.gt.notification_service.dto.PassRejectedEvent;
import com.gt.notification_service.dto.PasswordResetEvent; // Keep this if you have it
import com.gt.notification_service.dto.UserCreatedEvent;
import com.gt.notification_service.model.EmailAuditLog;
import com.gt.notification_service.model.EmailStatus;
import com.gt.notification_service.repository.EmailAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap; // <-- IMPORT THIS
import java.util.Map;          // <-- IMPORT THIS
import java.util.UUID;

@Service
public class NotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);
    private static final DateTimeFormatter FRIENDLY_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");

    private final EmailAuditLogRepository emailAuditLogRepository;
    private final EmailSenderService emailSenderService;

    public NotificationListener(EmailAuditLogRepository emailAuditLogRepository, EmailSenderService emailSenderService) {
        this.emailAuditLogRepository = emailAuditLogRepository;
        this.emailSenderService = emailSenderService;
    }

    // Inside NotificationListener.java

@RabbitListener(queues = "pass.approved.queue", errorHandler = "rabbitMQErrorHandler")
public void handlePassApproved(PassApprovedEvent event) {
    logger.info("Received PassApprovedEvent for pass ID: {}. Notifying employee and visitor.", event.getPassId());

    // --- VVV THIS IS THE NEW, UPGRADED EMPLOYEE NOTIFICATION VVV ---
    String employeeSubject = "Visitor Pass Request Approved: " + event.getVisitorName();
    
    // Create a map of details for the employee's email
    Map<String, String> employeeDetails = new LinkedHashMap<>();
    employeeDetails.put("Visitor Name", event.getVisitorName());
    employeeDetails.put("Pass Code", event.getPassCode());
    employeeDetails.put("Scheduled For", event.getVisitDateTime().format(FRIENDLY_FORMATTER));

    String employeeBody = createHtmlEmailTemplate(
        "Pass Request Approved",
        "Hello " + event.getEmployeeName() + ",",
        "Good news! The visitor pass you requested has been approved. The visitor has been sent a confirmation email with their pass code.",
        employeeDetails,
        "Please be ready to host your visitor at the scheduled time. Thank you."
    );
    processEmailNotification(event.getPassId(), event.getEmployeeEmail(), employeeSubject, employeeBody);

    // --- The visitor notification remains unchanged ---
    if (event.getVisitorEmail() != null && !event.getVisitorEmail().isEmpty()) {
        String visitorSubject = "Your Visitor Pass is Confirmed!";
        Map<String, String> visitorDetails = new LinkedHashMap<>();
        visitorDetails.put("PASS CODE", "<strong>" + event.getPassCode() + "</strong>");
        visitorDetails.put("Scheduled For", event.getVisitDateTime().format(FRIENDLY_FORMATTER));
        visitorDetails.put("Host Employee", event.getEmployeeName());
        
        String visitorBody = createHtmlEmailTemplate(
            "Visitor Pass Confirmed",
            "Dear " + event.getVisitorName() + ",",
            "Your visit has been confirmed. Please find the details below. Be ready to present your unique pass code to security upon arrival.",
            visitorDetails,
            "We look forward to welcoming you."
        );
        processEmailNotification(event.getPassId(), event.getVisitorEmail(), visitorSubject, visitorBody);
    }
}

    @RabbitListener(queues = "pass.rejected.queue", errorHandler = "rabbitMQErrorHandler")
    public void handlePassRejected(PassRejectedEvent event) {
        logger.info("Received PassRejectedEvent for pass ID: {}. Notifying employee and visitor.", event.getPassId());

        // --- 1. Notification for the Employee ---
        String employeeSubject = "Visitor Pass Request Status: Rejected";
        Map<String, String> employeeDetails = new LinkedHashMap<>();
        employeeDetails.put("Visitor Name", event.getVisitorName());
        employeeDetails.put("Rejection Reason", "<strong>" + event.getRejectionReason() + "</strong>");

        String employeeBody = createHtmlEmailTemplate(
            "Pass Request Rejected",
            "Hello,",
            "Unfortunately, your visitor pass request has been rejected. Please see the details below.",
            employeeDetails,
            "You may need to submit a new request with corrected information."
        );
        processEmailNotification(event.getPassId(), event.getEmployeeEmail(), employeeSubject, employeeBody);

        // VVV --- THIS IS THE NEW LOGIC --- VVV
        // --- 2. Notification for the Visitor ---
        // We need to get the visitor's email. Let's assume the event DTO is updated to include it.
       if (event.getVisitorEmail() != null && !event.getVisitorEmail().isEmpty()) {
    String visitorSubject = "Your Visitor Pass is Confirmed!";
    
    // We are creating a Map to hold the details for the email table
    Map<String, String> details = new LinkedHashMap<>();
    
    // VVV --- THIS IS THE MOST IMPORTANT PART --- VVV
    // We are adding the PASS CODE to the details map.
    // The <strong> tag makes it bold in the email.
    details.put("PASS CODE", "<strong>" + event.getPassCode() + "</strong>");
    
    // We are also adding other important info
    details.put("Scheduled For", event.getVisitDateTime().format(FRIENDLY_FORMATTER));
    details.put("Host Employee", event.getEmployeeName());
    
    // Now we pass these details to our HTML template builder
    String visitorBody = createHtmlEmailTemplate(
        "Visitor Pass Confirmed",
        "Dear " + event.getVisitorName() + ",",
        "Your visit has been confirmed. Please find the details below. Be ready to present your unique pass code to security upon arrival.",
        details,
        "We look forward to welcoming you."
    );
    
    // Finally, we process and send the email
    processEmailNotification(event.getPassId(), event.getVisitorEmail(), visitorSubject, visitorBody);
}
    }

   @RabbitListener(queues = "user.created.queue", errorHandler = "rabbitMQErrorHandler")
public void handleUserCreated(UserCreatedEvent event) {
    logger.info("Received UserCreatedEvent for new user: {}", event.getNewUserEmail());
    String subject = "Welcome to the Visitor Pass Management System!";

    // Create a map of details for the new user's email
    Map<String, String> details = new LinkedHashMap<>();
    details.put("Username / Email", "<strong>" + event.getNewUserEmail() + "</strong>");
    details.put("Assigned Role", event.getNewUserRole().replace("ROLE_", ""));
    details.put("Location", event.getTenantName());
    details.put("Account Created By", event.getCreatedByAdminName()); // <-- We'll need to add this field
    details.put("Login Page", "<a href='" + event.getLoginUrl() + "' style='color: #0d6efd;'>Click here to log in</a>");

    String body = createHtmlEmailTemplate(
        "Your Account is Ready!",
        "Welcome, " + event.getNewUserName() + "!",
        "An account has been created for you in the Visitor Pass Management System. You can now access the platform using your email address.",
        details,
        "<strong>Next Steps:</strong> Please obtain your temporary password from the administrator who created your account (" + event.getCreatedByAdminName() + "). For security, we recommend you change your password after your first login."
    );

    processEmailNotification(null, event.getNewUserEmail(), subject, body);
}

    // --- (Your other listeners like handlePassExpired can go here) ---

  // REPLACE the existing processEmailNotification method with this one

private void processEmailNotification(Long passId, String recipientAddress, String subject, String body) {
    logger.info("Step A: Starting email processing for recipient: {}", recipientAddress);
    
    EmailAuditLog auditLog = new EmailAuditLog();
    auditLog.setCorrelationId(UUID.randomUUID().toString());
    auditLog.setAssociatedPassId(passId);
    auditLog.setRecipientAddress(recipientAddress);
    auditLog.setSubject(subject);
    auditLog.setBody(body);
    auditLog.setStatus(EmailStatus.PENDING);
    auditLog.setCreatedAt(LocalDateTime.now());
    
    EmailAuditLog savedLog = emailAuditLogRepository.save(auditLog);
    logger.info("Step B: Saved initial PENDING audit log to database with ID: {}", savedLog.getId());

    try {
        logger.info("Step C: Attempting to send email via EmailSenderService...");
        
        // This is the most likely point of failure
        boolean wasSent = emailSenderService.sendEmail(recipientAddress, subject, body);
        
        logger.info("Step D: EmailSenderService call completed. Result (wasSent): {}", wasSent);

        if (wasSent) {
            savedLog.setStatus(EmailStatus.SENT);
        } else {
            savedLog.setStatus(EmailStatus.FAILED);
            savedLog.setFailureReason("Email provider (SMTP) returned 'false' without an exception.");
        }
    } catch (Throwable t) { // Changed from Exception to Throwable to catch EVERYTHING
        logger.error("!!! STEP C FAILED: A throwable was caught while sending email !!!", t);
        savedLog.setStatus(EmailStatus.FAILED);
        savedLog.setFailureReason(t.getMessage());
    }

    savedLog.setProcessedAt(LocalDateTime.now());
    emailAuditLogRepository.save(savedLog);
    logger.info("Step E: Final email audit log status '{}' saved to database.", savedLog.getStatus());
}
    
    // --- VVV This helper method is new, add it to the bottom of your class VVV ---
    private String createHtmlEmailTemplate(String title, String heading, String intro, Map<String, String> details, String outro) {
        StringBuilder detailsHtml = new StringBuilder();
        details.forEach((key, value) -> {
            detailsHtml.append("<tr><td style='padding: 8px; border-bottom: 1px solid #ddd; background-color: #f9f9f9;'><strong>")
                       .append(key)
                       .append(":</strong></td><td style='padding: 8px; border-bottom: 1px solid #ddd;'>")
                       .append(value)
                       .append("</td></tr>");
        });

        return "<!DOCTYPE html>" +
               "<html><head><style>" +
               "body{font-family: Arial, sans-serif; color: #333;}" +
               ".container{max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);}" +
               ".header{background-color: #003366; color: white; padding: 10px 20px; text-align: center; border-radius: 8px 8px 0 0;}" +
               ".content{padding: 20px;}" +
               ".details-table{width: 100%; border-collapse: collapse; margin: 20px 0;}" +
               ".footer{text-align: center; font-size: 12px; color: #888; margin-top: 20px;}" +
               "</style></head><body>" +
               "<div class='container'>" +
               "<div class='header'><h2>" + title + "</h2></div>" +
               "<div class='content'>" +
               "<p><b>" + heading + "</b></p>" +
               "<p>" + intro + "</p>" +
               "<table class='details-table'>" + detailsHtml.toString() + "</table>" +
               "<p>" + outro + "</p>" +
               "</div>" +
               "<div class='footer'><p>&copy; " + java.time.Year.now().getValue() + " Visitor Pass System</p></div>" +
               "</div></body></html>";
    }
}