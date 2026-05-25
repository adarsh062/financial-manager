package com.adarsh.financemanager.controller;

import com.adarsh.financemanager.dto.MonthlyReportResponse;
import com.adarsh.financemanager.dto.YearlyReportResponse;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.security.SecurityUtils;
import com.adarsh.financemanager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final SecurityUtils securityUtils;

    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @PathVariable int year,
            @PathVariable int month
    ) {
        User user = securityUtils.getCurrentUser();
        return ResponseEntity.ok(reportService.getMonthlyReport(user, year, month));
    }

    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(
            @PathVariable int year
    ) {
        User user = securityUtils.getCurrentUser();
        return ResponseEntity.ok(reportService.getYearlyReport(user, year));
    }
}
