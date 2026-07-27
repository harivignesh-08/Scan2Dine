package com.scan2dine.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CollegeResponse {
    private Long id;
    private String collegeName;
    private String collegeCode;
    private String logo;
    private String themeColor;
    private String email;
    private String phone;
    private String erpName;
    private String erpBaseUrl;
    private String erpApiKey;
    private String subscriptionPlan;
    private String status;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;
    private LocalDateTime createdAt;
}
