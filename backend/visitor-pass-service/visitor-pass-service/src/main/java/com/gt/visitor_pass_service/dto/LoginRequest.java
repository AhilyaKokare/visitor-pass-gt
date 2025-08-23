package com.gt.visitor_pass_service.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username; // Corresponds to email
    private String password;
}