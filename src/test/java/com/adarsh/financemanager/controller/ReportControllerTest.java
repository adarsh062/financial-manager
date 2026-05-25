package com.adarsh.financemanager.controller;

import com.adarsh.financemanager.dto.MonthlyReportResponse;
import com.adarsh.financemanager.dto.YearlyReportResponse;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.exception.GlobalExceptionHandler;
import com.adarsh.financemanager.security.SecurityUtils;
import com.adarsh.financemanager.service.ReportService;
import com.adarsh.financemanager.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("ReportController Tests")
class ReportControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ReportService reportService;
    @MockBean private SecurityUtils securityUtils;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        when(securityUtils.getCurrentUser()).thenReturn(user);
    }

    // ── GET /api/reports/monthly/{year}/{month} ───────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /api/reports/monthly/{year}/{month}: success → 200")
    void getMonthlyReport_returns200() throws Exception {
        MonthlyReportResponse response = MonthlyReportResponse.builder()
                .year(2025)
                .month(5)
                .totalIncome(Map.of("Salary", new BigDecimal("50000.00")))
                .totalExpenses(Map.of("Food", new BigDecimal("8000.00")))
                .netSavings(new BigDecimal("42000.00"))
                .build();

        when(reportService.getMonthlyReport(eq(user), eq(2025), eq(5))).thenReturn(response);

        mockMvc.perform(get("/api/reports/monthly/2025/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.month").value(5))
                .andExpect(jsonPath("$.totalIncome.Salary").value(50000.00))
                .andExpect(jsonPath("$.totalExpenses.Food").value(8000.00))
                .andExpect(jsonPath("$.netSavings").value(42000.00));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/reports/monthly/{year}/{month}: empty month → 200 with zeros")
    void getMonthlyReport_emptyMonth_returnsZeros() throws Exception {
        MonthlyReportResponse response = MonthlyReportResponse.builder()
                .year(2025).month(1)
                .totalIncome(Map.of())
                .totalExpenses(Map.of())
                .netSavings(BigDecimal.ZERO)
                .build();

        when(reportService.getMonthlyReport(eq(user), eq(2025), eq(1))).thenReturn(response);

        mockMvc.perform(get("/api/reports/monthly/2025/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netSavings").value(0))
                .andExpect(jsonPath("$.totalIncome").isEmpty())
                .andExpect(jsonPath("$.totalExpenses").isEmpty());
    }

    // ── GET /api/reports/yearly/{year} ────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /api/reports/yearly/{year}: success → 200")
    void getYearlyReport_returns200() throws Exception {
        YearlyReportResponse response = YearlyReportResponse.builder()
                .year(2025)
                .totalIncome(Map.of("Salary", new BigDecimal("600000.00")))
                .totalExpenses(Map.of("Food", new BigDecimal("96000.00")))
                .netSavings(new BigDecimal("504000.00"))
                .build();

        when(reportService.getYearlyReport(eq(user), eq(2025))).thenReturn(response);

        mockMvc.perform(get("/api/reports/yearly/2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.totalIncome.Salary").value(600000.00))
                .andExpect(jsonPath("$.totalExpenses.Food").value(96000.00))
                .andExpect(jsonPath("$.netSavings").value(504000.00));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/reports/yearly/{year}: empty year → 200 with zeros")
    void getYearlyReport_emptyYear_returnsZeros() throws Exception {
        YearlyReportResponse response = YearlyReportResponse.builder()
                .year(2024)
                .totalIncome(Map.of())
                .totalExpenses(Map.of())
                .netSavings(BigDecimal.ZERO)
                .build();

        when(reportService.getYearlyReport(eq(user), eq(2024))).thenReturn(response);

        mockMvc.perform(get("/api/reports/yearly/2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").isEmpty())
                .andExpect(jsonPath("$.totalExpenses").isEmpty())
                .andExpect(jsonPath("$.netSavings").value(0));
    }
}
