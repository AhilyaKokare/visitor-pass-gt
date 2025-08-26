package com.gt.visitor_pass_service.controller;

// --- THIS IS THE CORRECTED AND ADDED IMPORT SECTION ---
import com.gt.visitor_pass_service.dto.CreateUserRequest;
import com.gt.visitor_pass_service.dto.TenantDashboardResponse;
import com.gt.visitor_pass_service.dto.UpdateUserStatusRequest;
import com.gt.visitor_pass_service.dto.UserResponse;
import com.gt.visitor_pass_service.service.DashboardService; // CORRECTED: from .service, not .dto
import com.gt.visitor_pass_service.service.TenantSecurityService;
import com.gt.visitor_pass_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag; // ADDED: Missing import for @Tag
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
// --- END OF CORRECTIONS ---

@RestController
@RequestMapping("/api/tenants/{tenantId}/admin")
@Tag(name = "2. Tenant Admin", description = "APIs for Tenant Admins to manage their own location's users and view dashboards.")
public class TenantAdminController {

    private final UserService userService;
    private final TenantSecurityService tenantSecurityService;
    private final DashboardService dashboardService;

    public TenantAdminController(UserService userService, TenantSecurityService tenantSecurityService, DashboardService dashboardService) {
        this.userService = userService;
        this.tenantSecurityService = tenantSecurityService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Get Tenant Admin Dashboard")
    public ResponseEntity<TenantDashboardResponse> getDashboard(@PathVariable Long tenantId, HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        TenantDashboardResponse dashboardData = dashboardService.getTenantDashboardData(tenantId);
        return ResponseEntity.ok(dashboardData);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Get All Users in Tenant (Paginated)",
            description = "Retrieves a paginated list of all users within the Tenant Admin's assigned location. " +
                    "Supports sorting and pagination via query parameters (e.g., ?page=0&size=10&sort=name,asc)")
    public ResponseEntity<Page<UserResponse>> getUsersInTenant(
            @Parameter(description = "ID of the tenant to manage") @PathVariable Long tenantId,
            @PageableDefault(size = 10, sort = "name") Pageable pageable, // <-- ADD THIS
            HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        Page<UserResponse> users = userService.getUsersByTenant(tenantId, pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Create a New User")
    public ResponseEntity<UserResponse> createUser(
            @Parameter(description = "ID of the tenant where user will be created") @PathVariable Long tenantId,
            @Valid @RequestBody CreateUserRequest request,
            HttpServletRequest servletRequest) {
        
        System.out.println("=== CREATE USER REQUEST IN CONTROLLER ===");
        System.out.println("Request validated successfully. Proceeding to service layer...");

        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        System.out.println("Tenant access check passed");

        UserResponse response = userService.createUser(tenantId, request);
        System.out.println("User created successfully by service: " + response);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Activate or Deactivate a User")
    public ResponseEntity<UserResponse> updateUserStatus(
            @Parameter(description = "ID of the tenant") @PathVariable Long tenantId,
            @Parameter(description = "ID of the user to update") @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request,
            HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        UserResponse response = userService.updateUserStatus(userId, tenantId, request.isActive());
        return ResponseEntity.ok(response);
    }
}