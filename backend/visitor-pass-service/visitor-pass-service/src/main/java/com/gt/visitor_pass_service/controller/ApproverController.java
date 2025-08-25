package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.dto.RejectPassRequest;
import com.gt.visitor_pass_service.dto.VisitorPassResponse;
import com.gt.visitor_pass_service.service.TenantSecurityService;
import com.gt.visitor_pass_service.service.VisitorPassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants/{tenantId}/approvals")
// This class name should be ApproverController, not a typo
public class ApproverController {

    private final VisitorPassService visitorPassService;
    private final TenantSecurityService tenantSecurityService;

    public ApproverController(VisitorPassService visitorPassService, TenantSecurityService tenantSecurityService) {
        this.visitorPassService = visitorPassService;
        this.tenantSecurityService = tenantSecurityService;
    }

    // VVV --- THIS IS THE CRITICAL METHOD --- VVV
    // Ensure it exists exactly like this.
   @GetMapping
@PreAuthorize("hasAnyAuthority('ROLE_APPROVER', 'ROLE_TENANT_ADMIN')")
@Operation(summary = "Get Passes by Status", description = "Retrieves a paginated list of visitor passes with a specific status.")
public ResponseEntity<Page<VisitorPassResponse>> getPassesByStatus(
        @PathVariable Long tenantId,
        @RequestParam String status,
        // Manually request page and size instead of using Pageable
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        HttpServletRequest servletRequest) {
    
    tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
    
    // Manually create the Pageable object
    Pageable pageable = PageRequest.of(page, size);
    
    Page<VisitorPassResponse> response = visitorPassService.getPassesByStatus(tenantId, status, pageable);
    return ResponseEntity.ok(response);
}

    @PostMapping("/{passId}/approve")
    @PreAuthorize("hasAnyAuthority('ROLE_APPROVER', 'ROLE_TENANT_ADMIN')")
    public ResponseEntity<VisitorPassResponse> approvePass(
            @PathVariable Long tenantId,
            @PathVariable Long passId,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        String approverEmail = authentication.getName();
        VisitorPassResponse response = visitorPassService.approvePass(passId, approverEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{passId}/reject")
    @PreAuthorize("hasAnyAuthority('ROLE_APPROVER', 'ROLE_TENANT_ADMIN')")
    public ResponseEntity<VisitorPassResponse> rejectPass(
            @PathVariable Long tenantId,
            @PathVariable Long passId,
            @Valid @RequestBody RejectPassRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        String approverEmail = authentication.getName();
        VisitorPassResponse response = visitorPassService.rejectPass(passId, approverEmail, request.getReason());
        return ResponseEntity.ok(response);
    }
}