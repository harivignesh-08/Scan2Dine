package com.scan2dine.api.controller;

import com.scan2dine.api.dto.response.ApiResponse;
import com.scan2dine.api.dto.response.AttendanceResponse;
import com.scan2dine.api.entity.Attendance;
import com.scan2dine.api.repository.AttendanceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Attendance Logs", description = "Endpoints for viewing meal check-in logs and history")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;

    public AttendanceController(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @GetMapping
    @Operation(summary = "Get list of all attendance scan logs (both PRESENT and DUPLICATE_ATTEMPT)")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAllAttendance() {
        List<AttendanceResponse> response = attendanceRepository.findAll()
                .stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Attendance logs retrieved successfully.", response));
    }

    @GetMapping("/today")
    @Operation(summary = "Get list of all attendance scans recorded today")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getTodayAttendance() {
        List<AttendanceResponse> response = attendanceRepository.findByAttendanceDateOrderByScanTimeDesc(LocalDate.now())
                .stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Today's attendance logs retrieved successfully.", response));
    }

    @GetMapping("/student/{id}")
    @Operation(summary = "Get check-in attendance logs history for a specific student")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getStudentAttendance(@PathVariable Long id) {
        List<AttendanceResponse> response = attendanceRepository.findByStudentIdOrderByScanTimeDesc(id)
                .stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Student attendance logs retrieved successfully.", response));
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
