package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.dto.CreateTenantAndAdminRequest;
import com.gt.visitor_pass_service.dto.SuperAdminDashboardDTO;
import com.gt.visitor_pass_service.dto.TenantActivityDTO;
import com.gt.visitor_pass_service.dto.TenantDashboardInfo;
import com.gt.visitor_pass_service.service.SuperAdminDashboardService;
import com.gt.visitor_pass_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import com.gt.visitor_pass_service.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/super-admin")
@Tag(name = "1. Super Admin", description = "Global APIs for managing tenants and viewing system-wide analytics.")
public class SuperAdminController {

    private final UserService userService;
    private final SuperAdminDashboardService dashboardService; // <-- INJECT NEW SERVICE

    public SuperAdminController(UserService userService, SuperAdminDashboardService dashboardService) {
        this.userService = userService;
        this.dashboardService = dashboardService; // <-- INITIALIZE
    }

    // NEW / REPLACED ENDPOINT
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get Super Admin Analytics Dashboard", description = "Retrieves a complete system-wide dashboard including global stats, per-tenant activity, and recent passes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved dashboard data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Caller is not a Super Admin")
    })
    public ResponseEntity<SuperAdminDashboardDTO> getAnalyticsDashboard() {
        SuperAdminDashboardDTO dashboardData = dashboardService.getDashboardData();
        return ResponseEntity.ok(dashboardData);
    }

    // The createTenantAndAdmin method remains the same
    @PostMapping("/tenants")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a Location and its Admin", description = "Creates a new office location and its first primary administrator.")
    public ResponseEntity<TenantDashboardInfo> createTenantAndAdmin(
            @Valid @RequestBody CreateTenantAndAdminRequest request,
            Authentication authentication) {

        System.out.println("=== CREATE TENANT ENDPOINT CALLED ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Principal: " + authentication.getPrincipal());
        System.out.println("Authorities: " + authentication.getAuthorities());
        System.out.println("Name: " + authentication.getName());
        System.out.println("Request: " + request);

        String creatorName = authentication.getName();
        TenantDashboardInfo response = userService.createTenantAndAdmin(request, creatorName);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/test-auth")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Test Authentication", description = "Test endpoint to verify super admin authentication.")
    public ResponseEntity<Map<String, Object>> testAuth(Authentication authentication) {
        System.out.println("=== TEST AUTH ENDPOINT CALLED ===");
        System.out.println("Authentication: " + authentication);
        System.out.println("Principal: " + authentication.getPrincipal());
        System.out.println("Authorities: " + authentication.getAuthorities());
        System.out.println("Name: " + authentication.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        response.put("principal", authentication.getPrincipal().toString());
        response.put("authorities", authentication.getAuthorities().toString());
        response.put("name", authentication.getName());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/locations")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get All Locations (Paginated)", description = "Retrieves a paginated list of all locations with their activity data.")
    public ResponseEntity<Page<TenantActivityDTO>> getAllLocations(
            @PageableDefault(size = 10, sort = "tenantName") Pageable pageable) {
        Page<TenantActivityDTO> locations = dashboardService.getPaginatedTenantActivity(pageable);
        return ResponseEntity.ok(locations);
    }

    @DeleteMapping("/locations/{tenantId}/admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete Location Admin", description = "Deletes the primary administrator of a location.")
    public ResponseEntity<Map<String, Object>> deleteLocationAdmin(@PathVariable Long tenantId) {
        System.out.println("=== DELETE LOCATION ADMIN CONTROLLER ===");
        System.out.println("Tenant ID: " + tenantId);
        System.out.println("Request timestamp: " + java.time.LocalDateTime.now());

        Map<String, Object> response = new HashMap<>();

        // Validate input
        if (tenantId == null || tenantId <= 0) {
            System.err.println("Invalid tenant ID provided: " + tenantId);
            response.put("error", "Invalid tenant ID. Must be a positive number.");
            response.put("tenantId", tenantId);
            response.put("status", "invalid_input");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Log the deletion attempt
            System.out.println("Attempting to delete admin for tenant: " + tenantId);

            // Call the service method
            userService.deleteTenantAdmin(tenantId);

            // Success response
            System.out.println("✅ Location admin deleted successfully for tenant: " + tenantId);

            response.put("message", "Location administrator deleted successfully.");
            response.put("tenantId", tenantId);
            response.put("status", "success");
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            System.err.println("❌ Resource not found: " + e.getMessage());

            response.put("error", "Resource not found: " + e.getMessage());
            response.put("tenantId", tenantId);
            response.put("status", "not_found");
            response.put("details", "The specified tenant or admin does not exist");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (IllegalArgumentException e) {
            System.err.println("❌ Invalid argument: " + e.getMessage());

            response.put("error", "Invalid request: " + e.getMessage());
            response.put("tenantId", tenantId);
            response.put("status", "bad_request");

            return ResponseEntity.badRequest().body(response);

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            System.err.println("❌ Database constraint violation: " + e.getMessage());

            response.put("error", "Cannot delete admin due to existing dependencies");
            response.put("tenantId", tenantId);
            response.put("status", "constraint_violation");
            response.put("details", "Please remove all related records first");

            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (RuntimeException e) {
            System.err.println("❌ Runtime error: " + e.getMessage());
            e.printStackTrace();

            response.put("error", e.getMessage());
            response.put("tenantId", tenantId);
            response.put("status", "runtime_error");
            response.put("details", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();

            response.put("error", "Unexpected server error occurred");
            response.put("tenantId", tenantId);
            response.put("status", "server_error");
            response.put("details", e.getClass().getSimpleName() + ": " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}