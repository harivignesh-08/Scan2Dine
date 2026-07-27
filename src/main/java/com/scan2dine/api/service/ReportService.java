package com.scan2dine.api.service;

import com.scan2dine.api.dto.response.AttendanceResponse;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    List<AttendanceResponse> getDailyReport(LocalDate date);
    List<AttendanceResponse> getMonthlyReport(int year, int month);
    ByteArrayInputStream generatePdfReport(LocalDate startDate, LocalDate endDate);
    ByteArrayInputStream generateExcelReport(LocalDate startDate, LocalDate endDate);
}
