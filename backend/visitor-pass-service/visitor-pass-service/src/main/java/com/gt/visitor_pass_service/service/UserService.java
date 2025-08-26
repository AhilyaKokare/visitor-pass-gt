package com.gt.visitor_pass_service.service;

import com.gt.visitor_pass_service.dto.*;
import com.gt.visitor_pass_service.exception.AccessDeniedException;
import com.gt.visitor_pass_service.exception.ResourceNotFoundException;
import com.gt.visitor_pass_service.model.Tenant;
import com.gt.visitor_pass_service.model.User;
import com.gt.visitor_pass_service.repository.TenantRepository;
import com.gt.visitor_pass_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.gt.visitor_pass_service.config.RabbitMQConfig;
import com.gt.visitor_pass_service.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import com.gt.visitor_pass_service.util.ValidationUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class UserService { // Renamed from AdminService

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final RabbitTemplate rabbitTemplate;
    private final EmailService emailService;


    public UserService(TenantRepository tenantRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService, RabbitTemplate rabbitTemplate, EmailService emailService) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.rabbitTemplate = rabbitTemplate;
        this.emailService = emailService;
    }

    @Transactional
    public TenantDashboardInfo createTenantAndAdmin(CreateTenantAndAdminRequest request, String creatorName) {
        System.out.println("=== CREATE TENANT AND ADMIN REQUEST ===");
        System.out.println("Tenant Name: " + request.getTenantName());
        System.out.println("Location Details: " + request.getLocationDetails());
        System.out.println("Admin Name: " + request.getAdminName());
        System.out.println("Admin Email: " + request.getAdminEmail());
        System.out.println("Admin Contact: " + request.getAdminContact());
        System.out.println("Admin Address: " + request.getAdminAddress());
        System.out.println("Admin Gender: " + request.getAdminGender());
        System.out.println("Admin Department: " + request.getAdminDepartment());
        System.out.println("Creator Name: " + creatorName);

        // Validate email uniqueness for the admin
        validateEmailUniqueness(request.getAdminEmail(), null);

        // Validate mobile uniqueness and format for the admin
        validateMobileUniqueness(request.getAdminContact(), null);

        // Step 1: Create and save the Tenant
        Tenant tenant = new Tenant();
        tenant.setName(request.getTenantName());
        tenant.setLocationDetails(request.getLocationDetails());
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setCreatedBy(creatorName);
        Tenant savedTenant = tenantRepository.save(tenant);

        // Step 2: Create and save the Tenant Admin User
        User tenantAdmin = new User();
        tenantAdmin.setUniqueId(UUID.randomUUID().toString());
        tenantAdmin.setName(request.getAdminName());
        tenantAdmin.setEmail(ValidationUtil.normalizeEmail(request.getAdminEmail()));
        tenantAdmin.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        tenantAdmin.setContact(ValidationUtil.normalizeMobile(request.getAdminContact()));
        tenantAdmin.setRole("ROLE_TENANT_ADMIN");
        tenantAdmin.setActive(true);
        tenantAdmin.setJoiningDate(LocalDate.now()); // Automatically set joining date to today
        tenantAdmin.setDepartment(request.getAdminDepartment() != null ? request.getAdminDepartment() : "Administration"); // Default department for tenant admin
        tenantAdmin.setAddress(request.getAdminAddress());
        tenantAdmin.setGender(request.getAdminGender());
        tenantAdmin.setTenant(savedTenant); // Assign to the newly created tenant
        User savedAdmin = userRepository.save(tenantAdmin);

        // Step 2.1: Send comprehensive welcome email to the new tenant admin
        try {
            System.out.println("=== SENDING COMPREHENSIVE TENANT CREATION EMAIL ===");
            System.out.println("Tenant: " + savedTenant.getName());
            System.out.println("Admin: " + savedAdmin.getName());
            System.out.println("Email: " + savedAdmin.getEmail());

            emailService.sendTenantCreationWelcomeEmail(
                savedAdmin.getEmail(),           // To email
                savedAdmin.getName(),            // Admin name
                savedAdmin.getContact(),         // Admin contact
                savedTenant.getName(),           // Location name
                savedTenant.getLocationDetails(), // Location address
                request.getAdminPassword(),      // Original password (before encoding)
                creatorName                      // Creator name
            );

            System.out.println("✅ Comprehensive tenant creation email sent successfully!");
            System.out.println("📧 Email sent to: " + savedAdmin.getEmail());
            System.out.println("🏢 Location: " + savedTenant.getName());

        } catch (Exception e) {
            System.err.println("❌ Failed to send tenant creation email to: " + savedAdmin.getEmail());
            System.err.println("Error type: " + e.getClass().getSimpleName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the entire operation if email fails - just log the error
        }

        auditService.logEvent("TENANT_CREATED", null, savedTenant.getId(), null);
        auditService.logEvent("TENANT_ADMIN_CREATED", savedAdmin.getId(), savedTenant.getId(), null);

        // Step 3: Return a DTO representing the combined result
        return mapToTenantDashboardInfo(savedTenant, savedAdmin);
    }

    // --- NEW Method for Super Admin Dashboard ---
    public List<TenantDashboardInfo> getTenantDashboardInfo() {
        List<Tenant> tenants = tenantRepository.findAll();

        // Find all tenant admins to avoid N+1 queries
        List<User> tenantAdmins = userRepository.findByRole("ROLE_TENANT_ADMIN");
        Map<Long, User> tenantIdToAdminMap = tenantAdmins.stream()
                .filter(user -> user.getTenant() != null)
                .collect(Collectors.toMap(user -> user.getTenant().getId(), user -> user, (existing, replacement) -> existing)); // handle duplicates if any

        return tenants.stream()
                .filter(tenant -> !"Global Administration".equals(tenant.getName())) // Exclude the system tenant
                .map(tenant -> {
                    User admin = tenantIdToAdminMap.get(tenant.getId());
                    return mapToTenantDashboardInfo(tenant, admin);
                })
                .collect(Collectors.toList());
    }

    // --- Admin Functions (Moved from AdminService) ---

    public Tenant createTenant(CreateTenantRequest request) {
        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setLocationDetails(request.getLocationDetails());
        return tenantRepository.save(tenant);
    }

   public UserResponse createTenantAdmin(Long tenantId, CreateUserRequest request) {
    request.setRole("ROLE_TENANT_ADMIN");
    // A Tenant Admin is typically created by a Super Admin.
    // We provide a placeholder email here to satisfy the method signature.
    String placeholderSuperAdminEmail = "superadmin@system.com";
    return createUser(tenantId, request, placeholderSuperAdminEmail);
}
   @Transactional
public UserResponse createUser(Long tenantId, CreateUserRequest request, String adminEmail) {
    validateEmailUniqueness(request.getEmail(), null);
    validateMobileUniqueness(request.getContact(), null);

    Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

    User creatingAdmin = userRepository.findByEmail(adminEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Admin User", "email", adminEmail));

    User user = new User();
    user.setUniqueId(UUID.randomUUID().toString());
    user.setName(request.getName());
    user.setEmail(ValidationUtil.normalizeEmail(request.getEmail()));
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setContact(ValidationUtil.normalizeMobile(request.getContact()));
    user.setRole(request.getRole());
    user.setTenant(tenant);
    user.setActive(true);
    user.setJoiningDate(request.getJoiningDate() != null ? request.getJoiningDate() : LocalDate.now());
    user.setAddress(request.getAddress());
    user.setGender(request.getGender());
    user.setDepartment(request.getDepartment());

    User savedUser = userRepository.save(user);
    auditService.logEvent("USER_CREATED", creatingAdmin.getId(), tenantId, savedUser.getId());

    UserCreatedEvent event = new UserCreatedEvent(
            savedUser.getName(),
            savedUser.getEmail(),
            savedUser.getRole(),
            tenant.getName(),
            "http://localhost:4200/login",
            creatingAdmin.getName() // This now matches the updated DTO
    );
    rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_USER_CREATED, event);

    return mapToUserResponse(savedUser);
}

    public Page<UserResponse> getUsersInTenant(Long tenantId, Pageable pageable) {
    Page<User> userPage = userRepository.findByTenantId(tenantId, pageable);
    return userPage.map(this::mapToUserResponse);
}
    public UserResponse updateUserStatus(Long userId, Long tenantId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getTenant() == null || !user.getTenant().getId().equals(tenantId)) {
            throw new AccessDeniedException("User does not belong to the specified tenant.");
        }

        user.setActive(isActive);
        User savedUser = userRepository.save(user);
        auditService.logEvent(isActive ? "USER_ACTIVATED" : "USER_DEACTIVATED", savedUser.getId(), tenantId, null);
        return mapToUserResponse(savedUser);
    }

    // --- NEW Profile Management Functions ---

    public UserResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return mapToUserResponse(user);
    }

    public UserResponse updateUserProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Validate email uniqueness if email is being changed
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            validateEmailUniqueness(request.getEmail(), user.getId());
        }

        // Validate mobile uniqueness if mobile is being changed
        if (request.getContact() != null && !request.getContact().equals(user.getContact())) {
            validateMobileUniqueness(request.getContact(), user.getId());
        }

        // Update only the allowed fields
        if (request.getContact() != null) {
            user.setContact(ValidationUtil.normalizeMobile(request.getContact()));
        }
        if (request.getEmail() != null) {
            user.setEmail(ValidationUtil.normalizeEmail(request.getEmail()));
        }
        user.setAddress(request.getAddress());

        User updatedUser = userRepository.save(user);
        auditService.logEvent("PROFILE_UPDATED", user.getId(), user.getTenant().getId(), null);
        return mapToUserResponse(updatedUser);
    }

    private TenantDashboardInfo mapToTenantDashboardInfo(Tenant tenant, User admin) {
        return TenantDashboardInfo.builder()
                .tenantId(tenant.getId())
                .tenantName(tenant.getName())
                .locationDetails(tenant.getLocationDetails())
                .createdAt(tenant.getCreatedAt())
                .createdBy(tenant.getCreatedBy())
                .adminName(admin != null ? admin.getName() : "N/A")
                .adminEmail(admin != null ? admin.getEmail() : "N/A")
                .adminContact(admin != null ? admin.getContact() : "N/A")
                .adminIsActive(admin != null && admin.isActive())
                .build();
    }

    // --- Updated Helper Method ---
    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUniqueId(user.getUniqueId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setContact(user.getContact());
        response.setRole(user.getRole());
        response.setActive(user.isActive());
        response.setTenantId(user.getTenant().getId());
        response.setJoiningDate(user.getJoiningDate());
        response.setAddress(user.getAddress());
        response.setGender(user.getGender());
        response.setDepartment(user.getDepartment());
        return response;
    }

    @Transactional
    public void deleteTenantAdmin(Long tenantId) {
        System.out.println("=== DELETE TENANT ADMIN SERVICE ===");
        System.out.println("Tenant ID: " + tenantId);

        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("Invalid tenant ID: " + tenantId);
        }

        try {
            // Step 1: Check if tenant exists
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId.toString()));
            System.out.println("Found tenant: " + tenant.getName());

            // Step 2: Find the tenant admin for this tenant
            User tenantAdmin = userRepository.findFirstByTenantIdAndRole(tenantId, "ROLE_TENANT_ADMIN")
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant Admin not found for tenant", "tenantId", tenantId.toString()));

            System.out.println("Found tenant admin: " + tenantAdmin.getEmail() + " (ID: " + tenantAdmin.getId() + ")");

            // Step 3: Check for any dependent records that might prevent deletion
            // This is where the 500 error likely occurs - foreign key constraints

            // Step 4: Handle audit logging safely
            try {
                if (auditService != null) {
                    auditService.logEvent("TENANT_ADMIN_DELETED", tenantAdmin.getId(), tenantId, null);
                    System.out.println("Audit log created successfully");
                }
            } catch (Exception auditError) {
                System.err.println("Warning: Audit logging failed but continuing with deletion: " + auditError.getMessage());
                // Don't fail the entire operation for audit issues
            }

            // Step 5: Perform the actual deletion
            Long adminId = tenantAdmin.getId();
            String adminEmail = tenantAdmin.getEmail();

            // Delete the user record
            userRepository.deleteById(adminId);

            // Force the deletion to be committed immediately
            userRepository.flush();

            System.out.println("Successfully deleted tenant admin: " + adminEmail + " (ID: " + adminId + ") for tenant: " + tenantId);

            // Step 6: Verify deletion was successful
            boolean stillExists = userRepository.existsById(adminId);
            if (stillExists) {
                throw new RuntimeException("Deletion verification failed - admin still exists in database");
            }

            System.out.println("Deletion verified successfully");

        } catch (ResourceNotFoundException e) {
            System.err.println("Resource not found: " + e.getMessage());
            throw e;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            System.err.println("Database constraint violation: " + e.getMessage());
            throw new RuntimeException("Cannot delete admin due to existing dependencies. Please remove all related records first.", e);
        } catch (Exception e) {
            System.err.println("Unexpected error in deleteTenantAdmin: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete tenant admin: " + e.getMessage(), e);
        }
    }

    /**
     * Validates email format and uniqueness across all users in the system
     * @param email The email to validate
     * @param excludeUserId User ID to exclude from validation (for updates)
     * @throws IllegalArgumentException if email format is invalid or already exists
     */
    private void validateEmailUniqueness(String email, Long excludeUserId) {
        System.out.println("=== VALIDATING EMAIL FORMAT AND UNIQUENESS ===");
        System.out.println("Email: " + email);
        System.out.println("Exclude User ID: " + excludeUserId);

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        // Validate email format first
        String emailValidationError = ValidationUtil.getEmailValidationError(email);
        if (emailValidationError != null) {
            System.err.println("Email format validation failed: " + emailValidationError);
            throw new IllegalArgumentException(emailValidationError);
        }

        String normalizedEmail = ValidationUtil.normalizeEmail(email);
        System.out.println("Normalized Email: " + normalizedEmail);

        // Check if email already exists
        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            System.out.println("Found existing user with email: " + user.getEmail());
            System.out.println("Existing user ID: " + user.getId());
            System.out.println("Existing user role: " + user.getRole());
            System.out.println("Existing user tenant: " + (user.getTenant() != null ? user.getTenant().getName() : "No tenant"));

            // If we're updating a user, exclude their current record
            if (excludeUserId != null && user.getId().equals(excludeUserId)) {
                System.out.println("Email belongs to the user being updated, validation passed");
                return;
            }

            // Email is already taken by another user
            String errorMessage = String.format(
                "Email '%s' is already registered with another user account (Role: %s). " +
                "Each email address can only be associated with one user account across all roles.",
                email,
                user.getRole().replace("ROLE_", "")
            );

            System.err.println("Email validation failed: " + errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        System.out.println("Email validation passed - email is unique");
    }

    /**
     * Validates mobile number format and uniqueness across all users in the system
     * @param mobile The mobile number to validate
     * @param excludeUserId User ID to exclude from validation (for updates)
     * @throws IllegalArgumentException if mobile format is invalid or already exists
     */
    private void validateMobileUniqueness(String mobile, Long excludeUserId) {
        System.out.println("=== VALIDATING MOBILE FORMAT AND UNIQUENESS ===");
        System.out.println("Mobile: " + mobile);
        System.out.println("Exclude User ID: " + excludeUserId);

        // Allow empty mobile numbers (optional field)
        if (mobile == null || mobile.trim().isEmpty()) {
            System.out.println("Mobile number is empty, skipping validation");
            return;
        }

        // Validate mobile format
        String mobileValidationError = ValidationUtil.getMobileValidationError(mobile);
        if (mobileValidationError != null) {
            System.err.println("Mobile format validation failed: " + mobileValidationError);
            throw new IllegalArgumentException(mobileValidationError);
        }

        String normalizedMobile = ValidationUtil.normalizeMobile(mobile);
        System.out.println("Normalized Mobile: " + normalizedMobile);

        // Check if mobile already exists
        Optional<User> existingUser = userRepository.findByContact(normalizedMobile);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            System.out.println("Found existing user with mobile: " + user.getContact());
            System.out.println("Existing user ID: " + user.getId());
            System.out.println("Existing user email: " + user.getEmail());
            System.out.println("Existing user role: " + user.getRole());

            // If we're updating a user, exclude their current record
            if (excludeUserId != null && user.getId().equals(excludeUserId)) {
                System.out.println("Mobile belongs to the user being updated, validation passed");
                return;
            }

            // Mobile is already taken by another user
            String errorMessage = String.format(
                "Mobile number '%s' is already registered with another user account (%s). " +
                "Each mobile number can only be associated with one user account.",
                mobile,
                user.getEmail()
            );

            System.err.println("Mobile validation failed: " + errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        System.out.println("Mobile validation passed - mobile is unique");
    }
}