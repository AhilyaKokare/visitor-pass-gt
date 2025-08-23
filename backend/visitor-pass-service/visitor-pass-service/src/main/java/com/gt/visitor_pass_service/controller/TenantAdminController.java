package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.dto.*;
import com.gt.visitor_pass_service.service.DashboardService;
import com.gt.visitor_pass_service.service.TenantSecurityService;
import com.gt.visitor_pass_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;

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
    @Operation(summary = "Get Tenant Admin Dashboard", description = "Retrieves an aggregated dashboard of statistics, recent passes, and activity logs for the specified tenant.")
    public ResponseEntity<TenantDashboardResponse> getDashboard(@Parameter(description = "ID of the tenant to manage") @PathVariable Long tenantId, HttpServletRequest servletRequest) {
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
    @Operation(summary = "Create a New User", description = "Creates a new user (Employee, Approver, or Security) within the Tenant Admin's assigned location.")
    public ResponseEntity<UserResponse> createUser(@Parameter(description = "ID of the tenant where user will be created") @PathVariable Long tenantId, @Valid @RequestBody CreateUserRequest request, HttpServletRequest servletRequest) {
        System.out.println("=== CREATE USER REQUEST ===");
        System.out.println("Tenant ID: " + tenantId);
        System.out.println("Request: " + request);
        System.out.println("Authorization Header: " + servletRequest.getHeader("Authorization"));

        try {
            tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
            System.out.println("Tenant access check passed");

            // Manual validation
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Name cannot be empty");
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be empty");
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be empty");
            }
            if (request.getRole() == null || request.getRole().trim().isEmpty()) {
                throw new IllegalArgumentException("Role cannot be empty");
            }

            UserResponse response = userService.createUser(tenantId, request);
            System.out.println("User created successfully: " + response);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error creating user: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @Operation(summary = "Activate or Deactivate a User", description = "Updates the active status of a user within the Tenant Admin's location.")
    public ResponseEntity<UserResponse> updateUserStatus(
            @Parameter(description = "ID of the tenant") @PathVariable Long tenantId,
            @Parameter(description = "ID of the user to update") @PathVariable Long userId,
            @RequestBody UpdateUserStatusRequest request,
            HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        UserResponse response = userService.updateUserStatus(userId, tenantId, request.isActive());
        return ResponseEntity.ok(response);
    }
}