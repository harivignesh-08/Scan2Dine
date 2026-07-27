package com.scan2dine.api.controller;

import com.scan2dine.api.dto.response.ApiResponse;
import com.scan2dine.api.security.CustomUserDetails;
import com.scan2dine.api.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Dashboard Statistics", description = "Dynamic endpoint returning dashboard summaries customized by role (Super Admin vs. College Admin/Warden)")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Get dashboard analytics based on authenticated User Role")
    public ResponseEntity<ApiResponse<Object>> getDashboardMetrics() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            if ("SUPER_ADMIN".equalsIgnoreCase(userDetails.getRole())) {
                return ResponseEntity.ok(ApiResponse.success(
                        "Super Admin Dashboard metrics retrieved.", 
                        dashboardService.getSuperAdminDashboard()
                ));
            } else {
                return ResponseEntity.ok(ApiResponse.success(
                        "College Admin Dashboard metrics retrieved.", 
                        dashboardService.getCollegeDashboard()
                ));
            }
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Failed to resolve user context."));
    }
}
