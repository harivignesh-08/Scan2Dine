package com.scan2dine.api.service;

import com.scan2dine.api.dto.response.CollegeAdminDashboardResponse;
import com.scan2dine.api.dto.response.SuperAdminDashboardResponse;

public interface DashboardService {
    CollegeAdminDashboardResponse getCollegeDashboard();
    SuperAdminDashboardResponse getSuperAdminDashboard();
}
