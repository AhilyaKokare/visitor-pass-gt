package com.gt.visitor_pass_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDate;

@Data
@ToString
public class CreateUserRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @NotBlank(message = "Role cannot be blank")
    private String role; // e.g., "ROLE_EMPLOYEE", "ROLE_APPROVER", "ROLE_SECURITY"

    // --- NEW FIELDS ---
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate joiningDate;

    private String address;
    private String contact;
    private String gender;
    private String department;
}