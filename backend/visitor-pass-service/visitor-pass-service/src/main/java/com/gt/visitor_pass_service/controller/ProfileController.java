package com.gt.visitor_pass_service.controller;

import com.gt.visitor_pass_service.dto.UpdateProfileRequest;
import com.gt.visitor_pass_service.dto.UserResponse;
import com.gt.visitor_pass_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "7. User Profile", description = "APIs for users to manage their own profile information.")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get My Profile", description = "Retrieves the complete profile details for the currently authenticated user.")
    public ResponseEntity<UserResponse> getMyProfile(Authentication authentication) {
        String userEmail = authentication.getName();
        UserResponse userProfile = userService.getCurrentUserProfile(userEmail);
        return ResponseEntity.ok(userProfile);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update My Profile", description = "Allows the authenticated user to update their own contact information, email, and address.")
    public ResponseEntity<UserResponse> updateMyProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        String userEmail = authentication.getName();
        UserResponse updatedProfile = userService.updateUserProfile(userEmail, request);
        return ResponseEntity.ok(updatedProfile);
    }
}