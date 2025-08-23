package com.gt.visitor_pass_service.util;

import java.util.regex.Pattern;

/**
 * Utility class for validating email and mobile number formats
 */
public class ValidationUtil {

    // Email validation regex pattern (RFC 5322 compliant)
    private static final String EMAIL_REGEX = 
        "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";
    
    // Mobile number validation patterns for different formats
    private static final String MOBILE_REGEX_INTERNATIONAL = "^\\+[1-9]\\d{1,14}$"; // +1234567890 to +123456789012345
    private static final String MOBILE_REGEX_NATIONAL = "^[0-9]{10,15}$"; // 1234567890 to 123456789012345
    private static final String MOBILE_REGEX_WITH_SPACES = "^\\+?[0-9\\s\\-\\(\\)]{10,20}$"; // Allows spaces, dashes, parentheses
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern MOBILE_PATTERN_INTERNATIONAL = Pattern.compile(MOBILE_REGEX_INTERNATIONAL);
    private static final Pattern MOBILE_PATTERN_NATIONAL = Pattern.compile(MOBILE_REGEX_NATIONAL);
    private static final Pattern MOBILE_PATTERN_WITH_SPACES = Pattern.compile(MOBILE_REGEX_WITH_SPACES);

    /**
     * Validates email format
     * @param email The email to validate
     * @return true if email format is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String trimmedEmail = email.trim();
        
        // Check length constraints
        if (trimmedEmail.length() > 254) { // RFC 5321 limit
            return false;
        }
        
        // Check for basic format
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            return false;
        }
        
        // Additional checks
        String[] parts = trimmedEmail.split("@");
        if (parts.length != 2) {
            return false;
        }
        
        String localPart = parts[0];
        String domainPart = parts[1];
        
        // Local part should not exceed 64 characters
        if (localPart.length() > 64) {
            return false;
        }
        
        // Domain part should not exceed 253 characters
        if (domainPart.length() > 253) {
            return false;
        }
        
        // Domain should have at least one dot
        if (!domainPart.contains(".")) {
            return false;
        }
        
        return true;
    }

    /**
     * Validates mobile number format
     * @param mobile The mobile number to validate
     * @return true if mobile format is valid, false otherwise
     */
    public static boolean isValidMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return false;
        }
        
        String trimmedMobile = mobile.trim();
        
        // Remove all spaces, dashes, and parentheses for validation
        String cleanMobile = trimmedMobile.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Check if it matches international format (+1234567890)
        if (MOBILE_PATTERN_INTERNATIONAL.matcher(cleanMobile).matches()) {
            return true;
        }
        
        // Check if it matches national format (1234567890)
        if (MOBILE_PATTERN_NATIONAL.matcher(cleanMobile).matches()) {
            return true;
        }
        
        // Check if original format with spaces/dashes is valid
        if (MOBILE_PATTERN_WITH_SPACES.matcher(trimmedMobile).matches()) {
            // Ensure it has enough digits
            String digitsOnly = trimmedMobile.replaceAll("[^0-9]", "");
            return digitsOnly.length() >= 10 && digitsOnly.length() <= 15;
        }
        
        return false;
    }

    /**
     * Normalizes email by trimming and converting to lowercase
     * @param email The email to normalize
     * @return normalized email
     */
    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /**
     * Normalizes mobile number by removing spaces, dashes, and parentheses
     * @param mobile The mobile number to normalize
     * @return normalized mobile number
     */
    public static String normalizeMobile(String mobile) {
        if (mobile == null) {
            return null;
        }
        
        String trimmed = mobile.trim();
        
        // If it starts with +, keep the +, otherwise remove all non-digits
        if (trimmed.startsWith("+")) {
            return "+" + trimmed.substring(1).replaceAll("[^0-9]", "");
        } else {
            return trimmed.replaceAll("[^0-9]", "");
        }
    }

    /**
     * Gets detailed email validation error message
     * @param email The email to validate
     * @return error message if invalid, null if valid
     */
    public static String getEmailValidationError(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email cannot be empty";
        }
        
        String trimmedEmail = email.trim();
        
        if (trimmedEmail.length() > 254) {
            return "Email address is too long (maximum 254 characters)";
        }
        
        if (!trimmedEmail.contains("@")) {
            return "Email must contain @ symbol";
        }
        
        String[] parts = trimmedEmail.split("@");
        if (parts.length != 2) {
            return "Email format is invalid";
        }
        
        String localPart = parts[0];
        String domainPart = parts[1];
        
        if (localPart.isEmpty()) {
            return "Email local part cannot be empty";
        }
        
        if (localPart.length() > 64) {
            return "Email local part is too long (maximum 64 characters)";
        }
        
        if (domainPart.isEmpty()) {
            return "Email domain cannot be empty";
        }
        
        if (domainPart.length() > 253) {
            return "Email domain is too long (maximum 253 characters)";
        }
        
        if (!domainPart.contains(".")) {
            return "Email domain must contain at least one dot";
        }
        
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            return "Email format is invalid";
        }
        
        return null; // Valid
    }

    /**
     * Gets detailed mobile validation error message
     * @param mobile The mobile number to validate
     * @return error message if invalid, null if valid
     */
    public static String getMobileValidationError(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return "Mobile number cannot be empty";
        }
        
        String trimmedMobile = mobile.trim();
        String digitsOnly = trimmedMobile.replaceAll("[^0-9]", "");
        
        if (digitsOnly.length() < 10) {
            return "Mobile number must have at least 10 digits";
        }
        
        if (digitsOnly.length() > 15) {
            return "Mobile number cannot have more than 15 digits";
        }
        
        // Check for valid characters
        if (!trimmedMobile.matches("^[\\+0-9\\s\\-\\(\\)]+$")) {
            return "Mobile number contains invalid characters. Only digits, +, spaces, dashes, and parentheses are allowed";
        }
        
        if (!isValidMobile(mobile)) {
            return "Mobile number format is invalid. Use formats like: +1234567890, 1234567890, or (123) 456-7890";
        }
        
        return null; // Valid
    }
}
