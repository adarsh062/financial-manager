package com.adarsh.financemanager.service;

import com.adarsh.financemanager.dto.MonthlyReportResponse;
import com.adarsh.financemanager.dto.YearlyReportResponse;
import com.adarsh.financemanager.entity.User;

public interface ReportService {

    MonthlyReportResponse getMonthlyReport(User user, int year, int month);

    YearlyReportResponse getYearlyReport(User user, int year);
}
