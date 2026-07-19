package com.astra.codereview.service;

import com.astra.codereview.dto.AuthDtos.AuthResponse;
import com.astra.codereview.dto.AuthDtos.LoginRequest;
import com.astra.codereview.dto.AuthDtos.RegisterRequest;
import com.astra.codereview.entity.User;
import com.astra.codereview.exception.ApiExceptions;
import com.astra.codereview.repository.UserRepository;
import com.astra.codereview.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiExceptions.DuplicateEmailException("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail(), saved.getId());
        return AuthResponse.of(token, saved.getId(), saved.getName(), saved.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ApiExceptions.InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiExceptions.InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return AuthResponse.of(token, user.getId(), user.getName(), user.getEmail());
    }
}
