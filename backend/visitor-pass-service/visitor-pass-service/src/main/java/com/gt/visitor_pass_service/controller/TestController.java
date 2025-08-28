package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.dto.CreateUserRequest;
import com.gt.visitor_pass_service.dto.UserResponse;
import com.gt.visitor_pass_service.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
public class TestController {

    private final UserService userService;

    public TestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Test controller is working");
        return ResponseEntity.ok(response);
    }

    // Inside TestController.java

@PostMapping("/tenants/{tenantId}/users")
@PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')") // Changed to hasAuthority
public ResponseEntity<?> createUserSimple(@PathVariable Long tenantId, @RequestBody Map<String, Object> userData) {
    try {
        CreateUserRequest request = new CreateUserRequest();
        request.setName((String) userData.get("name"));
        request.setEmail((String) userData.get("email"));
        request.setPassword((String) userData.get("password"));
        request.setRole((String) userData.getOrDefault("role", "ROLE_EMPLOYEE"));
        // ... (setting other properties)
        
        // VVV --- THIS IS THE FIX --- VVV
        String placeholderAdminEmail = "test.admin@system.com";
        UserResponse response = userService.createUser(tenantId, request, placeholderAdminEmail);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
        
    } catch (Exception e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
}
