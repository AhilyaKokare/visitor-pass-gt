package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.config.security.JwtTokenProvider;
import com.gt.visitor_pass_service.dto.SecurityDashboardResponse;
import com.gt.visitor_pass_service.dto.VisitorPassResponse;
import com.gt.visitor_pass_service.service.TenantSecurityService;
import com.gt.visitor_pass_service.service.VisitorPassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/security")
@Tag(name = "6. Security", description = "APIs for Security personnel to manage visitor check-in/out and view the daily dashboard.")
public class SecurityController {

    private final VisitorPassService visitorPassService;
    private final TenantSecurityService tenantSecurityService;
    private final JwtTokenProvider tokenProvider;

    public SecurityController(VisitorPassService visitorPassService, TenantSecurityService tenantSecurityService, JwtTokenProvider tokenProvider) {
        this.visitorPassService = visitorPassService;
        this.tenantSecurityService = tenantSecurityService;
        this.tokenProvider = tokenProvider;
    }

    @GetMapping("/dashboard/today")
    @PreAuthorize("hasAnyRole('SECURITY', 'TENANT_ADMIN')")
    @Operation(summary = "Get Security Dashboard", description = "Retrieves a list of all visitors who are approved or have already checked in for the current day.")
    public ResponseEntity<List<SecurityDashboardResponse>> getTodaysVisitors(
            @Parameter(description = "ID of the tenant") @PathVariable Long tenantId,
            HttpServletRequest request) {
        tenantSecurityService.checkTenantAccess(request.getHeader("Authorization"), tenantId);
        List<SecurityDashboardResponse> visitors = visitorPassService.getTodaysVisitors(tenantId);
        return ResponseEntity.ok(visitors);
    }

    @GetMapping("/passes/search")
    @PreAuthorize("hasAnyRole('SECURITY', 'TENANT_ADMIN')")
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
    @PreAuthorize("hasAnyRole('SECURITY', 'TENANT_ADMIN')")
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
    @PreAuthorize("hasAnyRole('SECURITY', 'TENANT_ADMIN')")
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