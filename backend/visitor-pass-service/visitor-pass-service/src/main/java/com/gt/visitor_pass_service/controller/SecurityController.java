package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.config.security.JwtTokenProvider;
import com.gt.visitor_pass_service.dto.SecurityDashboardPageDTO;
import com.gt.visitor_pass_service.dto.VisitorPassResponse;
import com.gt.visitor_pass_service.service.TenantSecurityService;
import com.gt.visitor_pass_service.service.VisitorPassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    public SecurityController(VisitorPassService visitorPassService, TenantSecurityService tenantSecurityService, JwtTokenProvider tokenProvider) {
        this.visitorPassService = visitorPassService;
        this.tenantSecurityService = tenantSecurityService;
        this.tokenProvider = tokenProvider;
    }

    @GetMapping("/dashboard/today")
    @PreAuthorize("hasAnyRole('SECURITY', 'TENANT_ADMIN')")
    @Operation(summary = "Get Security Dashboard (Paginated)", description = "Retrieves paginated lists of visitors who are approved or have already checked in for the current day. Use qualifiers 'approved' and 'onSite' for pagination params (e.g., approved_page=0&onSite_page=1).")
    public ResponseEntity<SecurityDashboardPageDTO> getTodaysVisitorsPaginated(
            @Parameter(description = "ID of the tenant") @PathVariable Long tenantId,
            @Parameter(hidden = true) @Qualifier("approved") @PageableDefault(size = 5, sort = "visitDateTime") Pageable approvedPageable,
            @Parameter(hidden = true) @Qualifier("onSite") @PageableDefault(size = 5, sort = "visitDateTime") Pageable onSitePageable,
            HttpServletRequest request) {
        tenantSecurityService.checkTenantAccess(request.getHeader("Authorization"), tenantId);
        SecurityDashboardPageDTO visitors = visitorPassService.getTodaysVisitorsPaginated(tenantId, approvedPageable, onSitePageable);
        return ResponseEntity.ok(visitors);
    }

    @GetMapping("/passes/search")
    @PreAuthorize("hasAnyRole('SECURITY', 'TENANT_ADMIN')")
    @Operation(summary = "Search for a Pass by Code")
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
    @Operation(summary = "Check-In a Visitor")
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
    @Operation(summary = "Check-Out a Visitor")
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