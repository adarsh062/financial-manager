package com.adarsh.financemanager.service.impl;

import com.adarsh.financemanager.dto.CategorySummary;
import com.adarsh.financemanager.dto.MonthlyReportResponse;
import com.adarsh.financemanager.dto.YearlyReportResponse;
import com.adarsh.financemanager.entity.CategoryType;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportServiceImpl Tests")
class ReportServiceImplTest {

    @Mock private TransactionRepository transactionRepository;

    @InjectMocks private ReportServiceImpl reportService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
    }

    // ── getMonthlyReport ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getMonthlyReport: returns correct totals and category breakdowns")
    void getMonthlyReport_returnsAggregatedData() {
        CategorySummary salarySummary = new CategorySummary("Salary", CategoryType.INCOME, new BigDecimal("50000.00"));
        CategorySummary foodSummary   = new CategorySummary("Food",   CategoryType.EXPENSE, new BigDecimal("8000.00"));
        CategorySummary rentSummary   = new CategorySummary("Rent",   CategoryType.EXPENSE, new BigDecimal("12000.00"));

        when(transactionRepository.getMonthlyGroupedByCategory(eq(user), eq(2025), eq(5), eq(CategoryType.INCOME)))
                .thenReturn(List.of(salarySummary));
        when(transactionRepository.getMonthlyGroupedByCategory(eq(user), eq(2025), eq(5), eq(CategoryType.EXPENSE)))
                .thenReturn(List.of(foodSummary, rentSummary));

        MonthlyReportResponse response = reportService.getMonthlyReport(user, 2025, 5);

        assertThat(response.getYear()).isEqualTo(2025);
        assertThat(response.getMonth()).isEqualTo(5);
        assertThat(response.getTotalIncome()).hasSize(1);
        assertThat(response.getTotalExpenses()).hasSize(2);
        assertThat(response.getTotalIncome().get("Salary")).isEqualByComparingTo("50000.00");
        assertThat(response.getTotalExpenses().get("Food")).isEqualByComparingTo("8000.00");
        assertThat(response.getTotalExpenses().get("Rent")).isEqualByComparingTo("12000.00");
        assertThat(response.getNetSavings()).isEqualByComparingTo("30000.00");
    }

    @Test
    @DisplayName("getMonthlyReport: empty month returns zero totals")
    void getMonthlyReport_emptyMonth_returnsZeros() {
        when(transactionRepository.getMonthlyGroupedByCategory(eq(user), eq(2025), eq(1), eq(CategoryType.INCOME)))
                .thenReturn(List.of());
        when(transactionRepository.getMonthlyGroupedByCategory(eq(user), eq(2025), eq(1), eq(CategoryType.EXPENSE)))
                .thenReturn(List.of());

        MonthlyReportResponse response = reportService.getMonthlyReport(user, 2025, 1);

        assertThat(response.getTotalIncome()).isEmpty();
        assertThat(response.getTotalExpenses()).isEmpty();
        assertThat(response.getNetSavings()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getMonthlyReport: net savings is negative when expenses exceed income")
    void getMonthlyReport_negativeNetSavings() {
        CategorySummary income  = new CategorySummary("Salary", CategoryType.INCOME,  new BigDecimal("5000.00"));
        CategorySummary expense = new CategorySummary("Rent",   CategoryType.EXPENSE, new BigDecimal("8000.00"));

        when(transactionRepository.getMonthlyGroupedByCategory(eq(user), eq(2025), eq(3), eq(CategoryType.INCOME)))
                .thenReturn(List.of(income));
        when(transactionRepository.getMonthlyGroupedByCategory(eq(user), eq(2025), eq(3), eq(CategoryType.EXPENSE)))
                .thenReturn(List.of(expense));

        MonthlyReportResponse response = reportService.getMonthlyReport(user, 2025, 3);

        assertThat(response.getNetSavings()).isEqualByComparingTo("-3000.00");
    }

    // ── getYearlyReport ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getYearlyReport: returns correct yearly aggregated data")
    void getYearlyReport_returnsAggregatedData() {
        CategorySummary salaryYear = new CategorySummary("Salary", CategoryType.INCOME,  new BigDecimal("600000.00"));
        CategorySummary foodYear   = new CategorySummary("Food",   CategoryType.EXPENSE, new BigDecimal("96000.00"));

        when(transactionRepository.getYearlyGroupedByCategory(eq(user), eq(2025), eq(CategoryType.INCOME)))
                .thenReturn(List.of(salaryYear));
        when(transactionRepository.getYearlyGroupedByCategory(eq(user), eq(2025), eq(CategoryType.EXPENSE)))
                .thenReturn(List.of(foodYear));
        when(transactionRepository.getYearlyTotal(eq(user), eq(2025), eq(CategoryType.INCOME)))
                .thenReturn(new BigDecimal("600000.00"));
        when(transactionRepository.getYearlyTotal(eq(user), eq(2025), eq(CategoryType.EXPENSE)))
                .thenReturn(new BigDecimal("96000.00"));

        YearlyReportResponse response = reportService.getYearlyReport(user, 2025);

        assertThat(response.getYear()).isEqualTo(2025);
        assertThat(response.getTotalIncome()).hasSize(1);
        assertThat(response.getTotalExpenses()).hasSize(1);
        assertThat(response.getTotalIncome().get("Salary")).isEqualByComparingTo("600000.00");
        assertThat(response.getTotalExpenses().get("Food")).isEqualByComparingTo("96000.00");
        assertThat(response.getNetSavings()).isEqualByComparingTo("504000.00");
    }

    @Test
    @DisplayName("getYearlyReport: empty year returns zero totals")
    void getYearlyReport_emptyYear_returnsZeros() {
        when(transactionRepository.getYearlyGroupedByCategory(eq(user), eq(2024), eq(CategoryType.INCOME)))
                .thenReturn(List.of());
        when(transactionRepository.getYearlyGroupedByCategory(eq(user), eq(2024), eq(CategoryType.EXPENSE)))
                .thenReturn(List.of());
        when(transactionRepository.getYearlyTotal(eq(user), eq(2024), eq(CategoryType.INCOME)))
                .thenReturn(BigDecimal.ZERO);
        when(transactionRepository.getYearlyTotal(eq(user), eq(2024), eq(CategoryType.EXPENSE)))
                .thenReturn(BigDecimal.ZERO);

        YearlyReportResponse response = reportService.getYearlyReport(user, 2024);

        assertThat(response.getTotalIncome()).isEmpty();
        assertThat(response.getTotalExpenses()).isEmpty();
        assertThat(response.getNetSavings()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
