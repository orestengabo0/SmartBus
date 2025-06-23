package com.example.controller;

import com.example.service.UserService;
import com.example.dto.CreateOperatorRequest;
import com.example.dto.CreateAdminRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.dto.ProfileResponse;
import com.example.dto.UpdateProfileRequest;
import com.example.dto.ChangePasswordRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    // Implement view profile, update profile, change password endpoints here

    @PostMapping("/create-operator")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileResponse> createOperator(@RequestBody CreateOperatorRequest request) {
        return ResponseEntity.ok(userService.createOperator(request));
    }

    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ProfileResponse> createAdmin(@RequestBody CreateAdminRequest request) {
        return ResponseEntity.ok(userService.createAdmin(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(userDetails.getUsername(), request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }
}