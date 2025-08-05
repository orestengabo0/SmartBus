package com.example.service;

import com.example.exception.EmailAlreadyRegisteredException;
import com.example.exception.CredentialsException;
import com.example.exception.ResourceNotFoundException;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.dto.ProfileResponse;
import com.example.dto.UpdateProfileRequest;
import com.example.dto.ChangePasswordRequest;
import com.example.dto.CreateOperatorRequest;
import com.example.dto.CreateAdminRequest;
import com.example.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toProfileResponse(user);
    }

    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        userRepository.save(user);
        return toProfileResponse(user);
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
        User user = createUserFromRequest(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone(),
                Role.OPERATOR
        );
        return toProfileResponse(user);
    }

    public ProfileResponse createAdmin(CreateAdminRequest request) {
        User user = createUserFromRequest(
                request.getFullName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone(),
                Role.ADMIN
        );
        return toProfileResponse(user);
    }

    private ProfileResponse toProfileResponse(User user) {
        ProfileResponse profile = new ProfileResponse();
        profile.setId(user.getId());
        profile.setFullName(user.getFullName());
        profile.setEmail(user.getEmail());
        profile.setPhone(user.getPhone());
        profile.setRole(user.getRole().name());
        profile.setCreatedAt(user.getCreatedAt());
        return profile;
    }
}