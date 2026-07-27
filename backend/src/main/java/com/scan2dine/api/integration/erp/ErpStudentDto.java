package com.scan2dine.api.integration.erp;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErpStudentDto {
    private String name;
    private String rollNumber;
    private String department;
    private Integer year;
    private String phone;
    private String erpSource;
}
