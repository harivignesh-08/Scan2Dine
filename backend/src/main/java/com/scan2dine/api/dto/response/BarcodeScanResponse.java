package com.scan2dine.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarcodeScanResponse {
    private String status; // ACCESS_GRANTED, ACCESS_DENIED
    private String message;
    private String studentName;
    private String rollNumber;
    private String hostelName;
    private String roomNumber;
    private String mealName;
    private LocalDateTime scanTime;
}
