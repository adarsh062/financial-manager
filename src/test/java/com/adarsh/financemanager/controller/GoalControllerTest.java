package com.adarsh.financemanager.controller;

import com.adarsh.financemanager.dto.GoalRequest;
import com.adarsh.financemanager.dto.GoalResponse;
import com.adarsh.financemanager.dto.GoalUpdateRequest;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.exception.GlobalExceptionHandler;
import com.adarsh.financemanager.exception.ResourceNotFoundException;
import com.adarsh.financemanager.security.SecurityConfig;
import com.adarsh.financemanager.security.SecurityUtils;
import com.adarsh.financemanager.service.GoalService;
import com.adarsh.financemanager.service.impl.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GoalController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("GoalController Tests")
class GoalControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private GoalService goalService;
    @MockBean private SecurityUtils securityUtils;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private User user;
    private GoalResponse sampleResponse;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        when(securityUtils.getCurrentUser()).thenReturn(user);

        sampleResponse = GoalResponse.builder()
                .id(1L)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("50000.00"))
                .targetDate(LocalDate.now().plusYears(1))
                .startDate(LocalDate.now())
                .currentProgress(BigDecimal.ZERO)
                .progressPercentage(BigDecimal.ZERO)
                .remainingAmount(new BigDecimal("50000.00"))
                .build();
    }

    // ── POST /api/goals ───────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("POST /api/goals: success → 201")
    void createGoal_success_returns201() throws Exception {
        GoalRequest req = new GoalRequest();
        req.setGoalName("Emergency Fund");
        req.setTargetAmount(new BigDecimal("50000.00"));
        req.setTargetDate(LocalDate.now().plusYears(1));

        when(goalService.createGoal(any(), eq(user))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
                .andExpect(jsonPath("$.targetAmount").value(50000.00))
                .andExpect(jsonPath("$.progressPercentage").value(0));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/goals: past target date → 400")
    void createGoal_pastTargetDate_returns400() throws Exception {
        GoalRequest req = new GoalRequest();
        req.setGoalName("Old Goal");
        req.setTargetAmount(new BigDecimal("1000.00"));
        req.setTargetDate(LocalDate.now().minusDays(1)); // past date

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/goals: zero target amount → 400")
    void createGoal_zeroAmount_returns400() throws Exception {
        GoalRequest req = new GoalRequest();
        req.setGoalName("Savings");
        req.setTargetAmount(BigDecimal.ZERO);
        req.setTargetDate(LocalDate.now().plusYears(1));

        mockMvc.perform(post("/api/goals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/goals ────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /api/goals: returns list → 200")
    void getGoals_returns200() throws Exception {
        when(goalService.getGoals(user)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals.length()").value(1))
                .andExpect(jsonPath("$.goals[0].goalName").value("Emergency Fund"));
    }

    // ── GET /api/goals/{id} ───────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /api/goals/{id}: found → 200")
    void getGoalById_returns200() throws Exception {
        when(goalService.getGoalById(eq(1L), eq(user))).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/goals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/goals/{id}: not found → 404")
    void getGoalById_notFound_returns404() throws Exception {
        when(goalService.getGoalById(eq(99L), eq(user)))
                .thenThrow(new ResourceNotFoundException("Goal not found with id: 99"));

        mockMvc.perform(get("/api/goals/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Goal not found with id: 99"));
    }

    // ── PUT /api/goals/{id} ───────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("PUT /api/goals/{id}: success → 200")
    void updateGoal_success_returns200() throws Exception {
        GoalUpdateRequest req = new GoalUpdateRequest();
        req.setTargetAmount(new BigDecimal("75000.00"));
        req.setTargetDate(LocalDate.now().plusYears(2));

        GoalResponse updated = GoalResponse.builder()
                .id(1L).goalName("Emergency Fund")
                .targetAmount(new BigDecimal("75000.00"))
                .targetDate(LocalDate.now().plusYears(2))
                .startDate(LocalDate.now())
                .currentProgress(BigDecimal.ZERO)
                .progressPercentage(BigDecimal.ZERO)
                .remainingAmount(new BigDecimal("75000.00"))
                .build();

        when(goalService.updateGoal(eq(1L), any(), eq(user))).thenReturn(updated);

        mockMvc.perform(put("/api/goals/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
                .andExpect(jsonPath("$.targetAmount").value(75000.00));
    }

    // ── DELETE /api/goals/{id} ────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/goals/{id}: success → 200")
    void deleteGoal_success_returns200() throws Exception {
        doNothing().when(goalService).deleteGoal(eq(1L), eq(user));

        mockMvc.perform(delete("/api/goals/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/goals/{id}: not found → 404")
    void deleteGoal_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Goal not found with id: 99"))
                .when(goalService).deleteGoal(eq(99L), eq(user));

        mockMvc.perform(delete("/api/goals/99")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Goal not found with id: 99"));
    }
}
