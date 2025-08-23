import { Injectable } from '@angular/core';

export interface ValidationResult {
  isValid: boolean;
  errorMessage?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ValidationService {

  constructor() { }

  /**
   * Validates email format
   * @param email The email to validate
   * @returns ValidationResult with isValid and errorMessage
   */
  validateEmail(email: string): ValidationResult {
    if (!email || email.trim() === '') {
      return { isValid: false, errorMessage: 'Email is required' };
    }

    const trimmedEmail = email.trim();

    // Check length constraints
    if (trimmedEmail.length > 254) {
      return { isValid: false, errorMessage: 'Email address is too long (maximum 254 characters)' };
    }

    // Basic email regex pattern (RFC 5322 compliant)
    const emailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;

    if (!emailRegex.test(trimmedEmail)) {
      return { isValid: false, errorMessage: 'Please enter a valid email address' };
    }

    // Check for @ symbol
    if (!trimmedEmail.includes('@')) {
      return { isValid: false, errorMessage: 'Email must contain @ symbol' };
    }

    const parts = trimmedEmail.split('@');
    if (parts.length !== 2) {
      return { isValid: false, errorMessage: 'Email format is invalid' };
    }

    const [localPart, domainPart] = parts;

    if (localPart.length === 0) {
      return { isValid: false, errorMessage: 'Email local part cannot be empty' };
    }

    if (localPart.length > 64) {
      return { isValid: false, errorMessage: 'Email local part is too long (maximum 64 characters)' };
    }

    if (domainPart.length === 0) {
      return { isValid: false, errorMessage: 'Email domain cannot be empty' };
    }

    if (domainPart.length > 253) {
      return { isValid: false, errorMessage: 'Email domain is too long (maximum 253 characters)' };
    }

    if (!domainPart.includes('.')) {
      return { isValid: false, errorMessage: 'Email domain must contain at least one dot' };
    }

    return { isValid: true };
  }

  /**
   * Validates mobile number format
   * @param mobile The mobile number to validate
   * @param required Whether the mobile number is required
   * @returns ValidationResult with isValid and errorMessage
   */
  validateMobile(mobile: string, required: boolean = false): ValidationResult {
    if (!mobile || mobile.trim() === '') {
      if (required) {
        return { isValid: false, errorMessage: 'Mobile number is required' };
      }
      return { isValid: true }; // Optional field
    }

    const trimmedMobile = mobile.trim();

    // Remove all spaces, dashes, and parentheses for validation
    const digitsOnly = trimmedMobile.replace(/[^0-9]/g, '');

    if (digitsOnly.length < 10) {
      return { isValid: false, errorMessage: 'Mobile number must have at least 10 digits' };
    }

    if (digitsOnly.length > 15) {
      return { isValid: false, errorMessage: 'Mobile number cannot have more than 15 digits' };
    }

    // Check for valid characters (digits, +, spaces, dashes, parentheses)
    const validCharRegex = /^[\+0-9\s\-\(\)]+$/;
    if (!validCharRegex.test(trimmedMobile)) {
      return { isValid: false, errorMessage: 'Mobile number contains invalid characters. Only digits, +, spaces, dashes, and parentheses are allowed' };
    }

    // Check for valid mobile patterns
    const patterns = [
      /^\+[1-9]\d{1,14}$/, // International format: +1234567890
      /^[0-9]{10,15}$/, // National format: 1234567890
      /^\+?[0-9\s\-\(\)]{10,20}$/ // Format with spaces/dashes: (123) 456-7890
    ];

    const cleanMobile = trimmedMobile.replace(/[\s\-\(\)]/g, '');
    const hasValidPattern = patterns.some(pattern => pattern.test(cleanMobile));

    if (!hasValidPattern) {
      return { isValid: false, errorMessage: 'Mobile number format is invalid. Use formats like: +1234567890, 1234567890, or (123) 456-7890' };
    }

    return { isValid: true };
  }

  /**
   * Validates password strength
   * @param password The password to validate
   * @returns ValidationResult with isValid and errorMessage
   */
  validatePassword(password: string): ValidationResult {
    if (!password || password.trim() === '') {
      return { isValid: false, errorMessage: 'Password is required' };
    }

    if (password.length < 6) {
      return { isValid: false, errorMessage: 'Password must be at least 6 characters long' };
    }

    if (password.length > 128) {
      return { isValid: false, errorMessage: 'Password is too long (maximum 128 characters)' };
    }

    return { isValid: true };
  }

  /**
   * Validates name field
   * @param name The name to validate
   * @returns ValidationResult with isValid and errorMessage
   */
  validateName(name: string): ValidationResult {
    if (!name || name.trim() === '') {
      return { isValid: false, errorMessage: 'Name is required' };
    }

    const trimmedName = name.trim();

    if (trimmedName.length < 2) {
      return { isValid: false, errorMessage: 'Name must be at least 2 characters long' };
    }

    if (trimmedName.length > 100) {
      return { isValid: false, errorMessage: 'Name is too long (maximum 100 characters)' };
    }

    // Check for valid characters (letters, spaces, hyphens, apostrophes)
    const nameRegex = /^[a-zA-Z\s\-'\.]+$/;
    if (!nameRegex.test(trimmedName)) {
      return { isValid: false, errorMessage: 'Name can only contain letters, spaces, hyphens, and apostrophes' };
    }

    return { isValid: true };
  }

  /**
   * Normalizes email by trimming and converting to lowercase
   * @param email The email to normalize
   * @returns normalized email
   */
  normalizeEmail(email: string): string {
    if (!email) return '';
    return email.trim().toLowerCase();
  }

  /**
   * Normalizes mobile number by removing spaces, dashes, and parentheses
   * @param mobile The mobile number to normalize
   * @returns normalized mobile number
   */
  normalizeMobile(mobile: string): string {
    if (!mobile) return '';
    
    const trimmed = mobile.trim();
    
    // If it starts with +, keep the +, otherwise remove all non-digits
    if (trimmed.startsWith('+')) {
      return '+' + trimmed.substring(1).replace(/[^0-9]/g, '');
    } else {
      return trimmed.replace(/[^0-9]/g, '');
    }
  }

  /**
   * Validates all user form fields
   * @param userData The user data to validate
   * @returns Object with validation results for each field
   */
  validateUserForm(userData: any): { [key: string]: ValidationResult } {
    const results: { [key: string]: ValidationResult } = {};

    results['name'] = this.validateName(userData.name);
    results['email'] = this.validateEmail(userData.email);
    results['password'] = this.validatePassword(userData.password);
    results['mobile'] = this.validateMobile(userData.contact, false); // Mobile is optional

    return results;
  }

  /**
   * Checks if all validation results are valid
   * @param validationResults The validation results to check
   * @returns true if all validations passed, false otherwise
   */
  isFormValid(validationResults: { [key: string]: ValidationResult }): boolean {
    return Object.values(validationResults).every(result => result.isValid);
  }

  /**
   * Gets the first error message from validation results
   * @param validationResults The validation results to check
   * @returns first error message or null if all valid
   */
  getFirstErrorMessage(validationResults: { [key: string]: ValidationResult }): string | null {
    for (const result of Object.values(validationResults)) {
      if (!result.isValid && result.errorMessage) {
        return result.errorMessage;
      }
    }
    return null;
  }
}
