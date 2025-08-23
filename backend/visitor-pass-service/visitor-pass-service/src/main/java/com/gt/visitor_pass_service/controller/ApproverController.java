package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.dto.RejectPassRequest;
import com.gt.visitor_pass_service.dto.VisitorPassResponse;
import com.gt.visitor_pass_service.service.TenantSecurityService;
import com.gt.visitor_pass_service.service.VisitorPassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants/{tenantId}/approvals")
@Tag(name = "5. Approver", description = "APIs for Approvers to manage pending visitor pass requests.")
public class ApproverController {

    private final VisitorPassService visitorPassService;
    private final TenantSecurityService tenantSecurityService;

    public ApproverController(VisitorPassService visitorPassService, TenantSecurityService tenantSecurityService) {
        this.visitorPassService = visitorPassService;
        this.tenantSecurityService = tenantSecurityService;
    }

    @PostMapping("/{passId}/approve")
    @PreAuthorize("hasAnyRole('APPROVER', 'TENANT_ADMIN')")
    @Operation(summary = "Approve a Visitor Pass", description = "Changes the status of a 'PENDING' pass to 'APPROVED'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pass approved successfully"),
            @ApiResponse(responseCode = "404", description = "Pass not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<VisitorPassResponse> approvePass(
            @Parameter(description = "ID of the tenant") @PathVariable Long tenantId,
            @Parameter(description = "ID of the pass to approve") @PathVariable Long passId,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        String approverEmail = authentication.getName();
        VisitorPassResponse response = visitorPassService.approvePass(passId, approverEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{passId}/reject")
    @PreAuthorize("hasAnyRole('APPROVER', 'TENANT_ADMIN')")
    @Operation(summary = "Reject a Visitor Pass", description = "Changes the status of a 'PENDING' pass to 'REJECTED'. A reason is required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pass rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data (e.g., missing reason)"),
            @ApiResponse(responseCode = "404", description = "Pass not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<VisitorPassResponse> rejectPass(
            @Parameter(description = "ID of the tenant") @PathVariable Long tenantId,
            @Parameter(description = "ID of the pass to reject") @PathVariable Long passId,
            @Valid @RequestBody RejectPassRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        String approverEmail = authentication.getName();
        VisitorPassResponse response = visitorPassService.rejectPass(passId, approverEmail, request.getReason());
        return ResponseEntity.ok(response);
    }
}