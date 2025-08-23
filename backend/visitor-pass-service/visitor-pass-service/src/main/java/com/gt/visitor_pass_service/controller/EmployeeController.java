package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.dto.CreatePassRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/tenants/{tenantId}/passes")
@Tag(name = "4. Employee & Pass Management", description = "APIs for employees to create passes and view pass history.")
public class EmployeeController {

    private final VisitorPassService visitorPassService;
    private final TenantSecurityService tenantSecurityService;

    public EmployeeController(VisitorPassService visitorPassService, TenantSecurityService tenantSecurityService) {
        this.visitorPassService = visitorPassService;
        this.tenantSecurityService = tenantSecurityService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'TENANT_ADMIN')")
    @Operation(summary = "Create a New Visitor Pass", description = "Allows an employee or tenant admin to create a new visitor pass request for their location. The pass will be in a 'PENDING' state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pass request created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not authorized or accessing wrong tenant")
    })
    public ResponseEntity<VisitorPassResponse> createPass(
            @Parameter(description = "ID of the tenant where the pass is being created") @PathVariable Long tenantId,
            @Valid @RequestBody CreatePassRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        String userEmail = authentication.getName();
        VisitorPassResponse response = visitorPassService.createPass(tenantId, request, userEmail);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'TENANT_ADMIN')")
    @Operation(summary = "Get Personal Pass History (Paginated)",
            description = "Retrieves a paginated list of all visitor passes created by the currently authenticated user.")
    public ResponseEntity<Page<VisitorPassResponse>> getMyPassHistory(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) { // <-- ADD Pageable
        String userEmail = authentication.getName();
        Page<VisitorPassResponse> history = visitorPassService.getPassHistoryForUser(userEmail, pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'APPROVER', 'SECURITY', 'TENANT_ADMIN')")
    @Operation(summary = "Get All Passes in Tenant (Paginated)",
            description = "Retrieves a paginated list of all passes within a tenant. Intended for admins, approvers, or security.")
    public ResponseEntity<Page<VisitorPassResponse>> getPassesForTenant(
            @Parameter(description = "ID of the tenant") @PathVariable Long tenantId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest servletRequest) {
        tenantSecurityService.checkTenantAccess(servletRequest.getHeader("Authorization"), tenantId);
        Page<VisitorPassResponse> passes = visitorPassService.getPassesByTenant(tenantId, pageable);
        return ResponseEntity.ok(passes);
    }
}