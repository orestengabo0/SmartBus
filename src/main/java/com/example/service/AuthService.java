package com.example.service;

import com.example.exception.EmailAlreadyRegisteredException;
import com.example.exception.CredentialsException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.TokenException;
import com.example.model.RefreshToken;
import com.example.model.User;
import com.example.model.Role;
import com.example.repository.RefreshTokenRepository;
import com.example.repository.UserRepository;
import com.example.dto.RegisterRequest;
import com.example.dto.LoginRequest;
import com.example.dto.AuthResponse;
import com.example.smartbus.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest, HttpServletRequest req) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new EmailAlreadyRegisteredException("Email already registered");
        }
        User user = new User();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhone(registerRequest.getPhone());
        user.setRole(Role.USER);
        user.setCreatedAt(java.time.LocalDateTime.now());
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = createRefreshToken(user,req);
        userRepository.save(user);
        return new AuthResponse(accessToken, refreshToken.getToken(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request, HttpServletRequest req) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CredentialsException("Invalid credentials");
        }
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        RefreshToken refreshTokenEntity = createRefreshToken(user, req);
        userRepository.save(user);
        return new AuthResponse(accessToken, refreshTokenEntity.getToken(), user.getRole().name());
    }

    public RefreshToken createRefreshToken(User user, HttpServletRequest request) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshToken.setIssuedIp(request.getRemoteAddr());
        refreshToken.setDeviceInfo(request.getHeader("User-Agent"));
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(String token, HttpServletRequest req) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken == null)
            throw new ResourceNotFoundException("Refresh token not found");

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenException("Refresh token expired");
        }

        String currentIp = req.getRemoteAddr();
        String currentDevice = req.getHeader("User-Agent");
        if (!refreshToken.getIssuedIp().equals(currentIp) ||
                !refreshToken.getDeviceInfo().equals(currentDevice)) {
            // Potential token theft - consider logging this security event
            throw new SecurityException("Potential refresh token theft detected");
        }
        // Generate new access token
        User user = refreshToken.getUser();
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());

        //Issue new token and invalidate old one
        refreshTokenRepository.delete(refreshToken);
        RefreshToken refreshTokenEntity = createRefreshToken(user, req);

        return new AuthResponse(accessToken, refreshTokenEntity.getToken(), user.getRole().name());
    }
}