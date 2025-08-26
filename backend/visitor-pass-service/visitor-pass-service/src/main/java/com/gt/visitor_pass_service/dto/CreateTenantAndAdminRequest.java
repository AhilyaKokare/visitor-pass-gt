package com.gt.visitor_pass_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

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

    // --- NEW FIELD TO CAPTURE JOINING DATE ---
    @NotNull(message = "Admin joining date cannot be null")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate adminJoiningDate;

    // Additional admin fields (if needed for future expansion)
    private String adminAddress;
    private String adminGender;
    private String adminDepartment;
}