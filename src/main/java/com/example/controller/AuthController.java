package com.example.controller;

import com.example.dto.RefreshTokenRequest;
import com.example.service.AuthService;
import com.example.dto.RegisterRequest;
import com.example.dto.LoginRequest;
import com.example.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication and token management")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Operation(summary = "Register a new user",
            description = "Creates a new user account and returns authentication tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully registered",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already registered"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest req) {
        return ResponseEntity.ok(authService.register(registerRequest, req));
    }

    @Operation(summary = "User login",
            description = "Authenticates a user and returns access and refresh tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loqinRequest, HttpServletRequest req) {
        return ResponseEntity.ok(authService.login(loqinRequest, req));
    }

    @Operation(summary = "Refresh access token",
            description = "Uses a valid refresh token to generate a new access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New tokens generated successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token"),
            @ApiResponse(responseCode = "404", description = "Refresh token not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request, HttpServletRequest req) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken(), req));
    }
}