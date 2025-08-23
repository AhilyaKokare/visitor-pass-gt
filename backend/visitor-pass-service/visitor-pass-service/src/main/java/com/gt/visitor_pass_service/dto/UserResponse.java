package com.gt.visitor_pass_service.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UserResponse {
    private Long id;
    private String uniqueId; // <-- NEW
    private String name;
    private String email;
    private String contact;
    private String role;
    private boolean isActive;
    private Long tenantId;

    // --- NEW FIELDS ---
    private LocalDate joiningDate;
    private String address;
    private String gender;
    private String department;
}