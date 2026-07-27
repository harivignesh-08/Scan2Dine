package com.scan2dine.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CollegeAdminDashboardResponse {
    private long totalStudents;
    private long todayBreakfastCount;
    private long todayLunchCount;
    private long todayDinnerCount;
    private long todayTotalAttendance;
    private long duplicateScanAttempts;
    private double mealUtilizationPercentage;
    private List<AttendanceResponse> recentScans;
}
