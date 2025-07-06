package com.chyngyz.taskmanager.service;

import com.chyngyz.taskmanager.dto.AuthResponse;
import com.chyngyz.taskmanager.dto.RegisterRequest;
import com.chyngyz.taskmanager.entity.*;
import com.chyngyz.taskmanager.repository.RefreshTokenRepository;
import com.chyngyz.taskmanager.repository.UserRepository;
import com.chyngyz.taskmanager.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldCreateUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setFirstName("Test");
        request.setLastName("User");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(jwtUtil.generateToken(eq("testuser"), eq(Role.ADMIN))).thenReturn("access-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response.getToken());
        assertNotNull(response.getRefreshToken());

        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_shouldAuthenticateAndReturnTokens() {
        String username = "testuser";
        String password = "password";

        User user = User.builder()
                .username(username)
                .password("encoded")
                .role(Role.USER)
                .build();

        // корректный способ мока метода authenticate
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(mockAuth);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(username, Role.USER)).thenReturn("access-token");

        AuthResponse response = authService.login(username, password);

        assertEquals("access-token", response.getToken());
        assertNotNull(response.getRefreshToken());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_shouldThrowExceptionIfUserNotFound() {
        when(userRepository.findByUsername("nouser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                authService.login("nouser", "pass"));
    }

    @Test
    void refreshToken_shouldReturnAccessTokenIfValid() {
        String token = "refresh-token";
        User user = User.builder()
                .username("testuser")
                .role(Role.USER)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));
        when(jwtUtil.generateToken("testuser", Role.USER)).thenReturn("new-access-token");

        Optional<String> result = authService.refreshToken(token);
        assertTrue(result.isPresent());
        assertEquals("new-access-token", result.get());
    }

    @Test
    void logout_shouldRevokeToken() {
        String token = "refresh-token";

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .revoked(false)
                .user(User.builder().username("u").build())
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build();

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        authService.logout(token);

        assertTrue(refreshToken.isRevoked());
        verify(refreshTokenRepository).save(refreshToken);
    }
}
