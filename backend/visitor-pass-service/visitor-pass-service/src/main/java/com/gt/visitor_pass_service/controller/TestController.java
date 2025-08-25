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

    @PostMapping("/tenants/{tenantId}/users")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<?> createUserSimple(@PathVariable Long tenantId, @RequestBody Map<String, Object> userData) {
        try {
            System.out.println("=== TEST CREATE USER ===");
            System.out.println("Tenant ID: " + tenantId);
            System.out.println("User Data: " + userData);

            // Create a simple CreateUserRequest
            CreateUserRequest request = new CreateUserRequest();
            request.setName((String) userData.get("name"));
            request.setEmail((String) userData.get("email"));
            request.setPassword((String) userData.get("password"));
            request.setRole((String) userData.getOrDefault("role", "ROLE_EMPLOYEE"));
            request.setDepartment((String) userData.get("department"));
            request.setContact((String) userData.get("contact"));
            request.setGender((String) userData.get("gender"));
            request.setAddress((String) userData.get("address"));
            
            // Handle joining date
            String joiningDateStr = (String) userData.get("joiningDate");
            if (joiningDateStr != null && !joiningDateStr.isEmpty()) {
                request.setJoiningDate(LocalDate.parse(joiningDateStr));
            } else {
                request.setJoiningDate(LocalDate.now());
            }

            System.out.println("Parsed request: " + request);

         UserResponse response = userService.createUser(tenantId, request);
            System.out.println("User created successfully: " + response);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error in test create user: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("type", e.getClass().getSimpleName());
            
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
