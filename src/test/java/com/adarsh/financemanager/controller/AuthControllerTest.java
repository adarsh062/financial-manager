package com.adarsh.financemanager.controller;

import com.adarsh.financemanager.dto.LoginRequest;
import com.adarsh.financemanager.dto.LoginResponse;
import com.adarsh.financemanager.dto.LogoutResponse;
import com.adarsh.financemanager.dto.RegisterRequest;
import com.adarsh.financemanager.dto.RegisterResponse;
import com.adarsh.financemanager.exception.GlobalExceptionHandler;
import com.adarsh.financemanager.exception.ResourceAlreadyExistsException;
import com.adarsh.financemanager.security.SecurityConfig;
import com.adarsh.financemanager.service.AuthService;
import com.adarsh.financemanager.service.impl.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    // ── POST /api/auth/register ───────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/register: success → 201 with userId")
    void register_success_returns201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("user@example.com");
        req.setPassword("password123");
        req.setFullName("Test User");
        req.setPhoneNumber("9876543210");

        when(authService.register(any())).thenReturn(new RegisterResponse("User registered successfully", 1L));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("POST /api/auth/register: missing fields → 400")
    void register_missingFields_returns400() throws Exception {
        // Empty body — all @NotBlank/@NotNull fields fail validation
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register: duplicate username → 409")
    void register_duplicateUsername_returns409() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("existing@example.com");
        req.setPassword("password123");
        req.setFullName("Test User");
        req.setPhoneNumber("9876543210");

        when(authService.register(any()))
                .thenThrow(new ResourceAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User already exists"));
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login: success → 200")
    void login_success_returns200() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("user@example.com");
        req.setPassword("password123");

        when(authService.login(any(), any(), any())).thenReturn(new LoginResponse("Login successful"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    @DisplayName("POST /api/auth/login: bad credentials → 401")
    void login_badCredentials_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("user@example.com");
        req.setPassword("wrongpass");

        when(authService.login(any(), any(), any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    // ── POST /api/auth/logout ─────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/logout: success → 200")
    void logout_success_returns200() throws Exception {
        when(authService.logout()).thenReturn(new LogoutResponse("Logout successful"));

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }
}
