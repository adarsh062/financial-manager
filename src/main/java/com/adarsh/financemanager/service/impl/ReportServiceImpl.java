package com.adarsh.financemanager.service.impl;

import com.adarsh.financemanager.dto.CategorySummary;
import com.adarsh.financemanager.dto.MonthlyReportResponse;
import com.adarsh.financemanager.dto.YearlyReportResponse;
import com.adarsh.financemanager.entity.CategoryType;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.repository.TransactionRepository;
import com.adarsh.financemanager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;

    @Override
    public MonthlyReportResponse getMonthlyReport(User user, int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }

        Map<String, BigDecimal> incomeMap = toMap(
                transactionRepository.getMonthlyGroupedByCategory(user, year, month, CategoryType.INCOME));
        Map<String, BigDecimal> expensesMap = toMap(
                transactionRepository.getMonthlyGroupedByCategory(user, year, month, CategoryType.EXPENSE));

        BigDecimal totalIncome = sumMap(incomeMap);
        BigDecimal totalExpenses = sumMap(expensesMap);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return MonthlyReportResponse.builder()
                .year(year)
                .month(month)
                .totalIncome(incomeMap)
                .totalExpenses(expensesMap)
                .netSavings(netSavings)
                .build();
    }

    @Override
    public YearlyReportResponse getYearlyReport(User user, int year) {
        Map<String, BigDecimal> incomeMap = toMap(
                transactionRepository.getYearlyGroupedByCategory(user, year, CategoryType.INCOME));
        Map<String, BigDecimal> expensesMap = toMap(
                transactionRepository.getYearlyGroupedByCategory(user, year, CategoryType.EXPENSE));

        BigDecimal totalIncome = transactionRepository.getYearlyTotal(user, year, CategoryType.INCOME);
        BigDecimal totalExpenses = transactionRepository.getYearlyTotal(user, year, CategoryType.EXPENSE);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return YearlyReportResponse.builder()
                .year(year)
                .totalIncome(incomeMap)
                .totalExpenses(expensesMap)
                .netSavings(netSavings)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Converts a list of CategorySummary projections into a Map<categoryName, totalAmount>
     * e.g. {"Salary": 50000.00, "Freelance": 5000.00}
     */
    private Map<String, BigDecimal> toMap(List<CategorySummary> summaries) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (CategorySummary s : summaries) {
            map.put(s.getCategoryName(), s.getTotalAmount());
        }
        return map;
    }

    private BigDecimal sumMap(Map<String, BigDecimal> map) {
        return map.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
