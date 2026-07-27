package com.scan2dine.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentResponse {
    private Long id;
    private String name;
    private String rollNumber;
    private String department;
    private Integer year;
    private String phone;
    private Long hostelId;
    private String hostelName;
    private Long roomId;
    private String roomNumber;
    private String barcode;
    private String status;
}
