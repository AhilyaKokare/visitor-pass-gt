package com.gt.visitor_pass_service.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    // Only includes fields the user is allowed to change
    private String contact;

    @Email(message = "Email should be valid")
    private String email;

    private String address;
}