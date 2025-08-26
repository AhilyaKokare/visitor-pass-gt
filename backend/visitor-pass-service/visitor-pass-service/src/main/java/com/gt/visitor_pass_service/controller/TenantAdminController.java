package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.dto.*;
import com.gt.visitor_pass_service.service.DashboardService;
import com.gt.visitor_pass_service.service.TenantSecurityService;
import com.gt.visitor_pass_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // <-- THE IMPORT
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants/{tenantId}/admin")
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
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<TenantDashboardResponse> getDashboard(@PathVariable Long tenantId, HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        TenantDashboardResponse dashboardData = dashboardService.getTenantDashboardData(tenantId);
        return ResponseEntity.ok(dashboardData);
    }

    // VVV --- THIS IS THE CORRECTED GET USERS METHOD --- VVV
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<Page<UserResponse>> getUsersInTenant(
            @PathVariable Long tenantId,
            Pageable pageable,
            HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        // It now correctly calls the getUsersInTenant method in the service
        Page<UserResponse> users = userService.getUsersInTenant(tenantId, pageable);
        return ResponseEntity.ok(users);
    }

    // VVV --- THIS IS THE CORRECTED CREATE USER METHOD --- VVV
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @PathVariable Long tenantId,
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication, // It now gets the Authentication principal
            HttpServletRequest servletRequest) {
        
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        // It now gets the admin's email from the principal
        String adminEmail = authentication.getName();
        // It now correctly calls the createUser method with all 3 arguments
        UserResponse response = userService.createUser(tenantId, request, adminEmail);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long tenantId,
            @PathVariable Long userId,
            @RequestBody UpdateUserStatusRequest request,
            HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        UserResponse response = userService.updateUserStatus(userId, tenantId, request.isActive());
        return ResponseEntity.ok(response);
    }
}