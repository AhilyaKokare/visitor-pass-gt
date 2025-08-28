package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.dto.*;
import com.gt.visitor_pass_service.service.DashboardService;
import com.gt.visitor_pass_service.service.TenantSecurityService;
import com.gt.visitor_pass_service.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants/{tenantId}/admin")
@Tag(name = "2. Tenant Admin", description = "APIs for Tenant Admins...")
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
        return ResponseEntity.ok(dashboardService.getTenantDashboardData(tenantId));
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<Page<UserResponse>> getUsersInTenant(@PathVariable Long tenantId, Pageable pageable, HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        return ResponseEntity.ok(userService.getUsersByTenant(tenantId, pageable));
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<UserResponse> createUser(@PathVariable Long tenantId, @Valid @RequestBody CreateUserRequest request, Authentication authentication, HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        String adminEmail = authentication.getName();
        return new ResponseEntity<>(userService.createUser(tenantId, request, adminEmail), HttpStatus.CREATED);
    }

    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long tenantId, @PathVariable Long userId,
            @RequestBody UpdateUserStatusRequest request, HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        return ResponseEntity.ok(userService.updateUserStatus(userId, tenantId, request.isActive()));
    }
}