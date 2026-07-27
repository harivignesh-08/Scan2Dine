package com.scan2dine.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "College name is required")
    private String collegeName;

    @NotBlank(message = "College code is required")
    private String collegeCode;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    @NotBlank(message = "Admin username is required")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @NotBlank(message = "Admin password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    private String logo;
    private String themeColor;
    private String erpName;
    private String erpBaseUrl;
    private String erpApiKey;
}
