package com.scan2dine.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class AttendanceResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String rollNumber;
    private String department;
    private String hostelName;
    private String roomNumber;
    private String mealName;
    private LocalDateTime scanTime;
    private LocalDate attendanceDate;
    private String status;
    private String wardenUsername;
}
