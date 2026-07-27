package com.scan2dine.api.controller;

import com.scan2dine.api.dto.response.ApiResponse;
import com.scan2dine.api.dto.response.AttendanceResponse;
import com.scan2dine.api.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reports Generation", description = "Endpoints for daily, monthly, PDF, and Excel reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/daily")
    @Operation(summary = "Get Daily report summary in JSON format")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getDailyReport(
            @RequestParam(value = "date", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        LocalDate queryDate = date != null ? date : LocalDate.now();
        List<AttendanceResponse> report = reportService.getDailyReport(queryDate);
        return ResponseEntity.ok(ApiResponse.success("Daily report retrieved successfully.", report));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get Monthly registry report summary in JSON format")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getMonthlyReport(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {

        int qYear = year != null ? year : LocalDate.now().getYear();
        int qMonth = month != null ? month : LocalDate.now().getMonthValue();
        
        List<AttendanceResponse> report = reportService.getMonthlyReport(qYear, qMonth);
        return ResponseEntity.ok(ApiResponse.success("Monthly report retrieved successfully.", report));
    }

    @GetMapping("/pdf")
    @Operation(summary = "Download Meal Attendance PDF Report")
    public ResponseEntity<InputStreamResource> downloadPdfReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ByteArrayInputStream bis = reportService.generatePdfReport(startDate, endDate);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=scan2dine_report_" + startDate + "_to_" + endDate + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/excel")
    @Operation(summary = "Download Meal Attendance Excel (XLSX) Sheet")
    public ResponseEntity<InputStreamResource> downloadExcelReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ByteArrayInputStream bis = reportService.generateExcelReport(startDate, endDate);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=scan2dine_report_" + startDate + "_to_" + endDate + ".xlsx");

        MediaType mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(new InputStreamResource(bis));
    }
}
