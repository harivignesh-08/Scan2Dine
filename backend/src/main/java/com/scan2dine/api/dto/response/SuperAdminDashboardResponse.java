package com.scan2dine.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class SuperAdminDashboardResponse {
    private long totalColleges;
    private long totalStudents;
    private long activeSubscriptions;
    private double monthlyRevenue;
    private long todayTotalScans;
    private Map<String, Long> planDistribution; // e.g. "FREE": 5, "BASIC": 10, "PREMIUM": 2
}
