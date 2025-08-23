package com.gt.visitor_pass_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTenantAndAdminRequest {
    // Tenant Details
    @NotBlank(message = "Tenant name cannot be blank")
    private String tenantName;

    @NotBlank(message = "Location details cannot be blank")
    private String locationDetails;

    // Tenant Admin User Details
    @NotBlank(message = "Admin name cannot be blank")
    private String adminName;

    @NotBlank(message = "Admin email cannot be blank")
    @Email(message = "Admin email must be a valid email format")
    private String adminEmail;

    @NotBlank(message = "Admin password cannot be blank")
    private String adminPassword;

    private String adminContact;

    // Additional admin fields
    private String adminAddress;
    private String adminGender;
    private String adminDepartment;
}