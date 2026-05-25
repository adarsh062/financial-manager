package com.adarsh.financemanager.controller;

import com.adarsh.financemanager.dto.TransactionRequest;
import com.adarsh.financemanager.dto.TransactionResponse;
import com.adarsh.financemanager.dto.TransactionUpdateRequest;
import com.adarsh.financemanager.entity.CategoryType;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.exception.GlobalExceptionHandler;
import com.adarsh.financemanager.exception.ResourceNotFoundException;
import com.adarsh.financemanager.security.SecurityConfig;
import com.adarsh.financemanager.security.SecurityUtils;
import com.adarsh.financemanager.service.TransactionService;
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

@WebMvcTest(TransactionController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("TransactionController Tests")
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TransactionService transactionService;
    @MockBean private SecurityUtils securityUtils;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private User user;
    private TransactionResponse sampleResponse;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        when(securityUtils.getCurrentUser()).thenReturn(user);

        sampleResponse = new TransactionResponse(
                1L, new BigDecimal("500.00"), LocalDate.now(),
                "Groceries", "Food", CategoryType.EXPENSE
        );
    }

    // ── POST /api/transactions ────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("POST /api/transactions: success → 201")
    void createTransaction_success_returns201() throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("500.00"));
        req.setDate(LocalDate.now());
        req.setDescription("Groceries");
        req.setCategory("Food");

        when(transactionService.createTransaction(any(), eq(user))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.category").value("Food"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/transactions: future date → 400")
    void createTransaction_futureDate_returns400() throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("500.00"));
        req.setDate(LocalDate.now().plusDays(1)); // future date
        req.setCategory("Food");

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/transactions: zero amount → 400")
    void createTransaction_zeroAmount_returns400() throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(BigDecimal.ZERO);
        req.setDate(LocalDate.now());
        req.setCategory("Food");

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/transactions: invalid category → 404")
    void createTransaction_invalidCategory_returns404() throws Exception {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("500.00"));
        req.setDate(LocalDate.now());
        req.setCategory("InvalidCategory");

        when(transactionService.createTransaction(any(), eq(user)))
                .thenThrow(new ResourceNotFoundException("Category not found with name: InvalidCategory"));

        mockMvc.perform(post("/api/transactions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Category not found with name: InvalidCategory"));
    }

    // ── GET /api/transactions ─────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /api/transactions: returns list → 200")
    void getTransactions_returns200() throws Exception {
        when(transactionService.getTransactions(eq(user), any(), any(), any()))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(1))
                .andExpect(jsonPath("$.transactions[0].description").value("Groceries"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/transactions: with date filters → 200")
    void getTransactions_withFilters_returns200() throws Exception {
        when(transactionService.getTransactions(eq(user), any(), any(), any()))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/transactions")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(1));
    }

    // ── PUT /api/transactions/{id} ────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("PUT /api/transactions/{id}: success → 200")
    void updateTransaction_success_returns200() throws Exception {
        TransactionUpdateRequest req = new TransactionUpdateRequest();
        req.setAmount(new BigDecimal("999.00"));
        req.setDescription("Updated");

        TransactionResponse updated = new TransactionResponse(
                1L, new BigDecimal("999.00"), LocalDate.now(),
                "Updated", "Food", CategoryType.EXPENSE
        );
        when(transactionService.updateTransaction(eq(1L), any(), eq(user))).thenReturn(updated);

        mockMvc.perform(put("/api/transactions/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(999.00))
                .andExpect(jsonPath("$.description").value("Updated"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/transactions/{id}: not found → 404")
    void updateTransaction_notFound_returns404() throws Exception {
        TransactionUpdateRequest req = new TransactionUpdateRequest();
        req.setAmount(new BigDecimal("999.00"));

        when(transactionService.updateTransaction(eq(99L), any(), eq(user)))
                .thenThrow(new ResourceNotFoundException("Transaction not found with id: 99"));

        mockMvc.perform(put("/api/transactions/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Transaction not found with id: 99"));
    }

    // ── DELETE /api/transactions/{id} ─────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/transactions/{id}: success → 200")
    void deleteTransaction_success_returns200() throws Exception {
        doNothing().when(transactionService).deleteTransaction(eq(1L), eq(user));

        mockMvc.perform(delete("/api/transactions/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/transactions/{id}: not found → 404")
    void deleteTransaction_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Transaction not found with id: 99"))
                .when(transactionService).deleteTransaction(eq(99L), eq(user));

        mockMvc.perform(delete("/api/transactions/99")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Transaction not found with id: 99"));
    }
}
