package com.adarsh.financemanager.service.impl;

import com.adarsh.financemanager.dto.LoginRequest;
import com.adarsh.financemanager.dto.LoginResponse;
import com.adarsh.financemanager.dto.RegisterRequest;
import com.adarsh.financemanager.dto.RegisterResponse;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.exception.ResourceAlreadyExistsException;
import com.adarsh.financemanager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private HttpSession httpSession;
    @Mock private HttpServletRequest httpRequest;
    @Mock private HttpServletResponse httpResponse;

    @InjectMocks private AuthServiceImpl authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");
        registerRequest.setPhoneNumber("9876543210");
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register: success — saves user and returns response")
    void register_success() {
        when(userRepository.existsByUsername("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPwd");
        User savedUser = User.builder().id(1L).username("test@example.com")
                .password("hashedPwd").fullName("Test User").phoneNumber("9876543210").build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RegisterResponse response = authService.register(registerRequest);

        assertThat(response.getMessage()).isEqualTo("User registered successfully");
        assertThat(response.getUserId()).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register: duplicate username → throws ResourceAlreadyExistsException")
    void register_duplicateUsername_throwsException() {
        when(userRepository.existsByUsername("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: success — authenticates and returns response")
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("test@example.com");
        request.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        LoginResponse response = authService.login(request, httpRequest, httpResponse);

        assertThat(response.getMessage()).isEqualTo("Login successful");
    }

    // ── logout ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("logout: success — invalidates session")
    void logout_success() {
        var response = authService.logout();
        assertThat(response.getMessage()).isEqualTo("Logout successful");
        verify(httpSession).invalidate();
    }
}
