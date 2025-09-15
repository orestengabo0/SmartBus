package com.example.service;

import com.example.exception.EmailAlreadyRegisteredException;
import com.example.exception.CredentialsException;
import com.example.exception.ResourceNotFoundException;
import com.example.exception.UnauthorizedException;
import com.example.mappers.UserMapper;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.dto.responses.ProfileResponse;
import com.example.dto.requests.UpdateProfileRequest;
import com.example.dto.requests.ChangePasswordRequest;
import com.example.dto.requests.CreateOperatorRequest;
import com.example.dto.requests.CreateAdminRequest;
import com.example.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toProfileResponse(user);
    }

    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        userRepository.save(user);
        return userMapper.toProfileResponse(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new CredentialsException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User createUserFromRequest(String fullName, String email,
            String password, String phone, Role role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyRegisteredException("Email already registered");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setRole(role);
        user.setCreatedAt(java.time.LocalDateTime.now());

        return userRepository.save(user);
    }

    public ProfileResponse createOperator(CreateOperatorRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("You do not have permission to add operator");
        }
        User user = createUserFromRequest(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone(),
                Role.OPERATOR);
        return userMapper.toProfileResponse(user);
    }

    public ProfileResponse createAdmin(CreateAdminRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        if (currentUser.getRole() != Role.SUPER_ADMIN) {
            throw new UnauthorizedException("You do not have permission to add admin user");
        }
        User user = createUserFromRequest(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone(),
                Role.ADMIN);
        return userMapper.toProfileResponse(user);
    }

    public boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN;
    }

    public boolean isAdminOrOperator(User user) {
        return user.getRole() == Role.ADMIN ||
                user.getRole() == Role.SUPER_ADMIN ||
                user.getRole() == Role.OPERATOR;
    }
}