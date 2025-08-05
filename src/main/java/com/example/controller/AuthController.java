package com.example.controller;

import com.example.service.AuthService;
import com.example.dto.RegisterRequest;
import com.example.dto.LoginRequest;
import com.example.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest, HttpServletRequest req) {
        return ResponseEntity.ok(authService.register(registerRequest, req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loqinRequest, HttpServletRequest req) {
        return ResponseEntity.ok(authService.login( loqinRequest, req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody String refreshToken, HttpServletRequest req) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken, req));
    }
}