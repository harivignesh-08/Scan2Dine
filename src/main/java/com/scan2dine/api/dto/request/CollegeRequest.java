package com.scan2dine.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollegeRequest {

    @NotBlank(message = "College name is required")
    private String collegeName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private String logo;
    private String themeColor;
    
    private String erpName;
    private String erpBaseUrl;
    private String erpApiKey;

    private String subscriptionPlan; // FREE, BASIC, PREMIUM
    private String status;           // PENDING, APPROVED, SUSPENDED
    private java.time.LocalDateTime subscriptionStartDate;
    private java.time.LocalDateTime subscriptionEndDate;
}
