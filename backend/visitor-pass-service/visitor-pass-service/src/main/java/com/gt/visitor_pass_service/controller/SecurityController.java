package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.config.security.JwtTokenProvider;
import com.gt.visitor_pass_service.dto.VisitorPassResponse;
import com.gt.visitor_pass_service.service.TenantSecurityService;
import com.gt.visitor_pass_service.service.VisitorPassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

// VVV --- THESE ARE THE CORRECT IMPORTS --- VVV
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// ^^^ --- END OF CORRECT IMPORTS --- ^^^

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants/{tenantId}/security")
@Tag(name = "6. Security", description = "APIs for Security personnel to manage visitor check-in/out and view the daily dashboard.")
public class SecurityController {

    private final VisitorPassService visitorPassService;
    private final TenantSecurityService tenantSecurityService;
    private final JwtTokenProvider tokenProvider;

    // The constructor should only have these three dependencies
    public SecurityController(VisitorPassService visitorPassService, TenantSecurityService tenantSecurityService, JwtTokenProvider tokenProvider) {
        this.visitorPassService = visitorPassService;
        this.tenantSecurityService = tenantSecurityService;
        this.tokenProvider = tokenProvider;
    }

    // This is the new paginated method
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_SECURITY', 'ROLE_TENANT_ADMIN')")
    public ResponseEntity<Page<VisitorPassResponse>> getTodaysDashboardPaginated(
            @PathVariable Long tenantId,
            Pageable pageable,
            HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        Page<VisitorPassResponse> response = visitorPassService.getTodaysVisitorsPaginated(tenantId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/passes/search")
    @PreAuthorize("hasAnyAuthority('ROLE_SECURITY', 'ROLE_TENANT_ADMIN')") // Changed to hasAnyAuthority
    @Operation(summary = "Search for a Pass by Code", description = "Finds a specific visitor pass using its unique pass code.")
    public ResponseEntity<VisitorPassResponse> findPassByCode(
            @Parameter(description = "ID of the tenant") @PathVariable Long tenantId,
            @Parameter(description = "The 8-character unique pass code") @RequestParam String passCode,
            HttpServletRequest request) {
        tenantSecurityService.checkTenantAccess(request.getHeader("Authorization"), tenantId);
        VisitorPassResponse response = visitorPassService.findByPassCode(tenantId, passCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-in/{passId}")
    @PreAuthorize("hasAnyAuthority('ROLE_SECURITY', 'ROLE_TENANT_ADMIN')") // Changed to hasAnyAuthority
    @Operation(summary = "Check-In a Visitor", description = "Marks an 'APPROVED' pass as 'CHECKED_IN'.")
    public ResponseEntity<VisitorPassResponse> checkInVisitor(
            @Parameter(description = "ID of the tenant") @PathVariable Long tenantId,
            @Parameter(description = "ID of the pass to check-in") @PathVariable Long passId,
            HttpServletRequest request) {
        tenantSecurityService.checkTenantAccess(request.getHeader("Authorization"), tenantId);
        VisitorPassResponse response = visitorPassService.checkIn(passId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-out/{passId}")
    @PreAuthorize("hasAnyAuthority('ROLE_SECURITY', 'ROLE_TENANT_ADMIN')") // Changed to hasAnyAuthority
    @Operation(summary = "Check-Out a Visitor", description = "Marks a 'CHECKED_IN' pass as 'CHECKED_OUT'.")
    public ResponseEntity<VisitorPassResponse> checkOutVisitor(
            @Parameter(description = "ID of the tenant") @PathVariable Long tenantId,
            @Parameter(description = "ID of the pass to check-out") @PathVariable Long passId,
            HttpServletRequest request) {
        tenantSecurityService.checkTenantAccess(request.getHeader("Authorization"), tenantId);
        String token = request.getHeader("Authorization").substring(7);
        Long securityUserId = tokenProvider.getUserIdFromJWT(token);
        VisitorPassResponse response = visitorPassService.checkOut(passId, securityUserId);
        return ResponseEntity.ok(response);
    }
}