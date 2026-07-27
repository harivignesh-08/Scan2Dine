package com.scan2dine.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "colleges")
@Getter
@Setter
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "college_name", nullable = false, length = 150)
    private String collegeName;

    @Column(name = "college_code", nullable = false, unique = true, length = 50)
    private String collegeCode;

    @Column(name = "logo")
    private String logo;

    @Column(name = "theme_color", length = 50)
    private String themeColor = "#4F46E5";

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "erp_name", length = 100)
    private String erpName;

    @Column(name = "erp_base_url")
    private String erpBaseUrl;

    @Column(name = "erp_api_key")
    private String erpApiKey;

    @Column(name = "subscription_plan", nullable = false, length = 50)
    private String subscriptionPlan = "FREE"; // FREE, BASIC, PREMIUM

    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING"; // PENDING, APPROVED, SUSPENDED

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.subscriptionStartDate == null) {
            this.subscriptionStartDate = LocalDateTime.now();
        }
        if (this.subscriptionEndDate == null) {
            this.subscriptionEndDate = LocalDateTime.now().plusDays(30); // 30 days default trial
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
