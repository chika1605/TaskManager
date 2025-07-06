package com.chyngyz.taskmanager.service;

import com.chyngyz.taskmanager.dto.*;
import com.chyngyz.taskmanager.entity.*;
import com.chyngyz.taskmanager.repository.RefreshTokenRepository;
import com.chyngyz.taskmanager.repository.UserRepository;
import com.chyngyz.taskmanager.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthResponse register(RegisterRequest request) {
        logger.info("Attempting to register user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.ADMIN)
                .build();

        userRepository.save(user);

        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole());
        String refreshTokenStr = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        logger.info("Refresh token created for user '{}'", user.getUsername());

        return new AuthResponse(accessToken, refreshTokenStr);
    }

    public AuthResponse login(String username, String password) {
        logger.info("User '{}' attempting to log in", username);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        logger.info("User '{}' logged in successfully", username);

        String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRole());
        String refreshTokenStr = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        logger.info("Refresh token generated for '{}'", username);

        return new AuthResponse(accessToken, refreshTokenStr);
    }

    public Optional<String> refreshToken(String refreshToken) {
        logger.info("Attempting to refresh access token using refresh token: {}", refreshToken);
        return refreshTokenRepository.findByToken(refreshToken)
                .filter(token -> !token.isRevoked())
                .filter(token -> token.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(token -> {
                    User user = token.getUser();
                    logger.info("New access token issued for user '{}'", user.getUsername());
                    return jwtUtil.generateToken(user.getUsername(), user.getRole());
                });
    }

    public void logout(String refreshToken) {
        logger.info("Attempting to logout using refresh token: {}", refreshToken);
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            logger.info("Refresh token revoked for user '{}'", token.getUser().getUsername());
        });
    }
}
