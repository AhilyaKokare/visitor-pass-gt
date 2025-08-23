package com.gt.visitor_pass_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@visitorpass.com}")
    private String fromEmail;

    @Value("${app.name:Visitor Pass System}")
    private String appName;

    @Value("${app.url:http://localhost:4200}")
    private String appUrl;

    /**
     * Sends welcome email to newly created tenant admin
     */
    public void sendTenantAdminWelcomeEmail(String toEmail, String adminName, String locationName,
                                          String password, String createdBy) {
        try {
            System.out.println("=== SENDING TENANT ADMIN WELCOME EMAIL ===");
            System.out.println("To: " + toEmail);
            System.out.println("Admin Name: " + adminName);
            System.out.println("Location: " + locationName);
            System.out.println("Created By: " + createdBy);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to " + appName + " - Tenant Admin Account Created");

            String htmlContent = buildTenantAdminWelcomeEmailContent(adminName, locationName, 
                                                                   toEmail, password, createdBy);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Welcome email sent successfully to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send welcome email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception to avoid breaking user creation process
        } catch (Exception e) {
            System.err.println("Unexpected error sending email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends comprehensive welcome email to newly created tenant admin with full details
     */
    public void sendTenantCreationWelcomeEmail(String toEmail, String adminName, String adminContact,
                                             String locationName, String locationAddress,
                                             String password, String createdBy) {
        try {
            System.out.println("=== SENDING COMPREHENSIVE TENANT CREATION EMAIL ===");
            System.out.println("To: " + toEmail);
            System.out.println("Admin Name: " + adminName);
            System.out.println("Admin Contact: " + adminContact);
            System.out.println("Location: " + locationName);
            System.out.println("Location Address: " + locationAddress);
            System.out.println("Created By: " + createdBy);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🎉 Welcome to " + appName + " - Your Location & Admin Account Created!");

            String htmlContent = buildComprehensiveTenantWelcomeEmail(adminName, adminContact,
                                                                    locationName, locationAddress,
                                                                    toEmail, password, createdBy);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✅ Comprehensive tenant creation email sent successfully to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Failed to send tenant creation email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error sending tenant creation email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Builds HTML content for tenant admin welcome email
     */
    private String buildTenantAdminWelcomeEmailContent(String adminName, String locationName, 
                                                     String email, String password, String createdBy) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Welcome to %s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 2px solid #007bff; }
                    .logo { font-size: 24px; font-weight: bold; color: #007bff; margin-bottom: 10px; }
                    .welcome-title { font-size: 20px; color: #333; margin: 0; }
                    .content { margin-bottom: 30px; }
                    .info-box { background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #007bff; }
                    .credentials { background: #fff3cd; padding: 15px; border-radius: 5px; border: 1px solid #ffeaa7; margin: 20px 0; }
                    .button { display: inline-block; padding: 12px 30px; background: #007bff; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; font-size: 14px; }
                    .important { color: #dc3545; font-weight: bold; }
                    .success { color: #28a745; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🏢 %s</div>
                        <h1 class="welcome-title">Welcome to Your Admin Account!</h1>
                    </div>
                    
                    <div class="content">
                        <p>Dear <strong>%s</strong>,</p>
                        
                        <p>Congratulations! Your Tenant Administrator account has been successfully created for <strong>%s</strong>.</p>
                        
                        <div class="info-box">
                            <h3>📋 Account Details</h3>
                            <p><strong>Name:</strong> %s</p>
                            <p><strong>Email:</strong> %s</p>
                            <p><strong>Role:</strong> Tenant Administrator</p>
                            <p><strong>Location:</strong> %s</p>
                            <p><strong>Account Created:</strong> %s</p>
                            <p><strong>Created By:</strong> %s</p>
                        </div>
                        
                        <div class="credentials">
                            <h3>🔐 Login Credentials</h3>
                            <p><strong>Email:</strong> %s</p>
                            <p><strong>Temporary Password:</strong> <code>%s</code></p>
                            <p class="important">⚠️ Please change your password after first login for security.</p>
                        </div>
                        
                        <h3>🚀 Getting Started</h3>
                        <p>As a Tenant Administrator, you can:</p>
                        <ul>
                            <li>✅ Manage users in your location</li>
                            <li>✅ Create employee, approver, and security accounts</li>
                            <li>✅ Monitor visitor pass activities</li>
                            <li>✅ View location dashboard and analytics</li>
                            <li>✅ Update your profile and settings</li>
                        </ul>
                        
                        <div style="text-align: center;">
                            <a href="%s" class="button">🔗 Login to Your Account</a>
                        </div>
                        
                        <div class="info-box">
                            <h3>📞 Need Help?</h3>
                            <p>If you have any questions or need assistance:</p>
                            <ul>
                                <li>Contact your Super Administrator</li>
                                <li>Check the user guide in the application</li>
                                <li>Use the help section in your dashboard</li>
                            </ul>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>This email was sent automatically by %s.</p>
                        <p>Please do not reply to this email.</p>
                        <p><small>© 2024 %s. All rights reserved.</small></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                appName, appName, adminName, locationName, adminName, email, locationName, 
                currentDate, createdBy, email, password, appUrl, appName, appName
            );
    }

    /**
     * Builds comprehensive HTML content for tenant creation welcome email
     */
    private String buildComprehensiveTenantWelcomeEmail(String adminName, String adminContact,
                                                      String locationName, String locationAddress,
                                                      String email, String password, String createdBy) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>🎉 Welcome to %s - Location Created!</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); }
                    .container { max-width: 650px; margin: 0 auto; background: white; border-radius: 15px; box-shadow: 0 10px 30px rgba(0,0,0,0.2); overflow: hidden; }
                    .header { background: linear-gradient(135deg, #007bff 0%%, #0056b3 100%%); color: white; text-align: center; padding: 40px 30px; }
                    .logo { font-size: 28px; font-weight: bold; margin-bottom: 10px; }
                    .welcome-title { font-size: 24px; margin: 0; opacity: 0.95; }
                    .content { padding: 40px 30px; }
                    .celebration { text-align: center; font-size: 48px; margin: 20px 0; }
                    .info-box { background: linear-gradient(135deg, #f8f9fa 0%%, #e9ecef 100%%); padding: 25px; border-radius: 10px; margin: 25px 0; border-left: 5px solid #007bff; }
                    .credentials { background: linear-gradient(135deg, #fff3cd 0%%, #ffeaa7 100%%); padding: 20px; border-radius: 10px; border: 2px solid #ffc107; margin: 25px 0; }
                    .location-details { background: linear-gradient(135deg, #d1ecf1 0%%, #bee5eb 100%%); padding: 20px; border-radius: 10px; border-left: 5px solid #17a2b8; margin: 25px 0; }
                    .button { display: inline-block; padding: 15px 35px; background: linear-gradient(135deg, #007bff 0%%, #0056b3 100%%); color: white; text-decoration: none; border-radius: 8px; margin: 25px 0; font-weight: bold; box-shadow: 0 4px 15px rgba(0,123,255,0.3); }
                    .button:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,123,255,0.4); }
                    .footer { text-align: center; margin-top: 40px; padding-top: 30px; border-top: 2px solid #eee; color: #666; font-size: 14px; }
                    .important { color: #dc3545; font-weight: bold; }
                    .success { color: #28a745; font-weight: bold; }
                    .feature-list { list-style: none; padding: 0; }
                    .feature-list li { padding: 8px 0; }
                    .feature-list li:before { content: "✅ "; color: #28a745; font-weight: bold; }
                    .contact-info { background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">🏢 %s</div>
                        <h1 class="welcome-title">🎉 Congratulations! Your Location is Ready!</h1>
                    </div>

                    <div class="content">
                        <div class="celebration">🎊 🎉 🎊</div>

                        <p>Dear <strong>%s</strong>,</p>

                        <p><strong>Fantastic news!</strong> Your new location <strong>"%s"</strong> has been successfully created in our Visitor Pass System, and you have been appointed as the <strong>Tenant Administrator</strong>!</p>

                        <div class="location-details">
                            <h3>🏢 Your New Location Details</h3>
                            <p><strong>📍 Location Name:</strong> %s</p>
                            <p><strong>🗺️ Address:</strong> %s</p>
                            <p><strong>📅 Created Date:</strong> %s</p>
                            <p><strong>👤 Created By:</strong> %s</p>
                            <p><strong>🎯 Status:</strong> <span class="success">Active & Ready</span></p>
                        </div>

                        <div class="info-box">
                            <h3>👤 Your Administrator Account Details</h3>
                            <p><strong>👨‍💼 Name:</strong> %s</p>
                            <p><strong>📧 Email:</strong> %s</p>
                            <p><strong>📱 Contact:</strong> %s</p>
                            <p><strong>🔑 Role:</strong> Tenant Administrator</p>
                            <p><strong>⚡ Status:</strong> <span class="success">Active</span></p>
                        </div>

                        <div class="credentials">
                            <h3>🔐 Your Login Credentials</h3>
                            <div class="contact-info">
                                <p><strong>🌐 Login Email:</strong> <code>%s</code></p>
                                <p><strong>🔑 Temporary Password:</strong> <code>%s</code></p>
                            </div>
                            <p class="important">⚠️ IMPORTANT: Please change your password immediately after your first login for security purposes!</p>
                        </div>

                        <h3>🚀 What You Can Do Now</h3>
                        <p>As a Tenant Administrator, you have full control over your location:</p>
                        <ul class="feature-list">
                            <li><strong>👥 User Management:</strong> Create and manage employee, approver, and security accounts</li>
                            <li><strong>🎫 Visitor Pass Control:</strong> Monitor and manage all visitor pass activities</li>
                            <li><strong>📊 Dashboard Analytics:</strong> View comprehensive reports and statistics</li>
                            <li><strong>🔧 Location Settings:</strong> Configure location-specific preferences</li>
                            <li><strong>👮‍♂️ Security Oversight:</strong> Manage security personnel and protocols</li>
                            <li><strong>✅ Approval Workflows:</strong> Set up and manage visitor approval processes</li>
                        </ul>

                        <div style="text-align: center;">
                            <a href="%s" class="button">🔗 Access Your Admin Dashboard</a>
                        </div>

                        <div class="info-box">
                            <h3>📞 Need Help Getting Started?</h3>
                            <p><strong>We're here to help you succeed!</strong></p>
                            <ul>
                                <li>📖 Check the comprehensive user guide in your dashboard</li>
                                <li>💬 Use the help section for step-by-step tutorials</li>
                                <li>📧 Contact your Super Administrator for any questions</li>
                                <li>🎯 Start by creating your first employee account</li>
                            </ul>
                        </div>

                        <div class="credentials">
                            <h3>🎯 Quick Start Checklist</h3>
                            <ul>
                                <li>✅ Location created successfully</li>
                                <li>✅ Admin account activated</li>
                                <li>🔲 Login and change password</li>
                                <li>🔲 Explore your dashboard</li>
                                <li>🔲 Create your first employee account</li>
                                <li>🔲 Set up visitor approval workflow</li>
                            </ul>
                        </div>
                    </div>

                    <div class="footer">
                        <p><strong>Welcome to the %s family!</strong></p>
                        <p>This email was sent automatically by %s on %s.</p>
                        <p>Please do not reply to this email.</p>
                        <p><small>© 2024 %s. All rights reserved.</small></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                appName, appName, adminName, locationName, locationName, locationAddress,
                currentDate, createdBy, adminName, email, adminContact, email, password,
                appUrl, appName, appName, currentDate, appName
            );
    }

    /**
     * Sends simple text email (fallback method)
     */
    public void sendSimpleEmail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            System.out.println("Simple email sent successfully to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Failed to send simple email to: " + toEmail);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends password reset email
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        try {
            String subject = "Password Reset Request - " + appName;
            String resetUrl = appUrl + "/reset-password?token=" + resetToken;
            
            String content = String.format("""
                Dear %s,
                
                You have requested to reset your password for %s.
                
                Click the link below to reset your password:
                %s
                
                This link will expire in 24 hours.
                
                If you did not request this password reset, please ignore this email.
                
                Best regards,
                %s Team
                """, userName, appName, resetUrl, appName);
            
            sendSimpleEmail(toEmail, subject, content);
            
        } catch (Exception e) {
            System.err.println("Failed to send password reset email to: " + toEmail);
            e.printStackTrace();
        }
    }
}
