package com.scan2dine.api.service.impl;

import com.scan2dine.api.dto.response.AttendanceResponse;
import com.scan2dine.api.dto.response.CollegeAdminDashboardResponse;
import com.scan2dine.api.dto.response.SuperAdminDashboardResponse;
import com.scan2dine.api.entity.Attendance;
import com.scan2dine.api.entity.College;
import com.scan2dine.api.repository.AttendanceRepository;
import com.scan2dine.api.repository.CollegeRepository;
import com.scan2dine.api.repository.StudentRepository;
import com.scan2dine.api.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final CollegeRepository collegeRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;

    public DashboardServiceImpl(CollegeRepository collegeRepository,
                                StudentRepository studentRepository,
                                AttendanceRepository attendanceRepository) {
        this.collegeRepository = collegeRepository;
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeAdminDashboardResponse getCollegeDashboard() {
        LocalDate today = LocalDate.now();

        long totalStudents = studentRepository.count();
        long breakfast = attendanceRepository.countTodayScanByMealNameAndStatus(today, "Breakfast", "PRESENT");
        long lunch = attendanceRepository.countTodayScanByMealNameAndStatus(today, "Lunch", "PRESENT");
        long dinner = attendanceRepository.countTodayScanByMealNameAndStatus(today, "Dinner", "PRESENT");
        
        long totalAttendanceToday = attendanceRepository.countByAttendanceDateAndStatus(today, "PRESENT");
        long duplicateAttempts = attendanceRepository.countByAttendanceDateAndStatus(today, "DUPLICATE_ATTEMPT");

        // Calculate utilization % = (actual scans today / possible scans today) * 100
        double utilization = 0.0;
        if (totalStudents > 0) {
            double possibleScans = totalStudents * 3.0; // 3 meals a day
            utilization = (totalAttendanceToday / possibleScans) * 100.0;
        }

        // Get 10 recent scans
        List<AttendanceResponse> recentScans = attendanceRepository.findByAttendanceDateOrderByScanTimeDesc(today)
                .stream()
                .limit(10)
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());

        return CollegeAdminDashboardResponse.builder()
                .totalStudents(totalStudents)
                .todayBreakfastCount(breakfast)
                .todayLunchCount(lunch)
                .todayDinnerCount(dinner)
                .todayTotalAttendance(totalAttendanceToday)
                .duplicateScanAttempts(duplicateAttempts)
                .mealUtilizationPercentage(Math.round(utilization * 100.0) / 100.0)
                .recentScans(recentScans)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SuperAdminDashboardResponse getSuperAdminDashboard() {
        LocalDate today = LocalDate.now();

        // Since SUPER_ADMIN has collegeId = null, these counts automatically query across all tenants
        long totalColleges = collegeRepository.count();
        long totalStudentsGlobal = studentRepository.count();
        long totalScansTodayGlobal = attendanceRepository.countByAttendanceDate(today);

        List<College> colleges = collegeRepository.findAll();
        long activeSubscriptions = 0;
        double monthlyRevenue = 0.0;
        
        Map<String, Long> planDistribution = new HashMap<>();
        planDistribution.put("FREE", 0L);
        planDistribution.put("BASIC", 0L);
        planDistribution.put("PREMIUM", 0L);

        for (College college : colleges) {
            if ("APPROVED".equalsIgnoreCase(college.getStatus())) {
                activeSubscriptions++;
                String plan = college.getSubscriptionPlan().toUpperCase();
                planDistribution.put(plan, planDistribution.getOrDefault(plan, 0L) + 1);

                // Basic = $49/mo, Premium = $149/mo, Free = $0/mo
                if ("BASIC".equals(plan)) {
                    monthlyRevenue += 49.00;
                } else if ("PREMIUM".equals(plan)) {
                    monthlyRevenue += 149.00;
                }
            }
        }

        return SuperAdminDashboardResponse.builder()
                .totalColleges(totalColleges)
                .totalStudents(totalStudentsGlobal)
                .activeSubscriptions(activeSubscriptions)
                .monthlyRevenue(monthlyRevenue)
                .todayTotalScans(totalScansTodayGlobal)
                .planDistribution(planDistribution)
                .build();
    }

    private AttendanceResponse mapToAttendanceResponse(Attendance attendance) {
        AttendanceResponse res = new AttendanceResponse();
        res.setId(attendance.getId());
        res.setStudentId(attendance.getStudent().getId());
        res.setStudentName(attendance.getStudent().getName());
        res.setRollNumber(attendance.getStudent().getRollNumber());
        res.setDepartment(attendance.getStudent().getDepartment());
        res.setHostelName(attendance.getStudent().getHostel() != null ? attendance.getStudent().getHostel().getName() : "N/A");
        res.setRoomNumber(attendance.getStudent().getRoom() != null ? attendance.getStudent().getRoom().getRoomNumber() : "N/A");
        res.setMealName(attendance.getMeal().getMealName());
        res.setScanTime(attendance.getScanTime());
        res.setAttendanceDate(attendance.getAttendanceDate());
        res.setStatus(attendance.getStatus());
        res.setWardenUsername(attendance.getWarden() != null ? attendance.getWarden().getUsername() : "System");
        return res;
    }
}
