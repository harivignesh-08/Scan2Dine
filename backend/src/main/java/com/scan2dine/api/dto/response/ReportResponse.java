package com.scan2dine.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReportResponse {
    private Long id;
    private String reportType;
    private String generatedByUsername;
    private String filePath;
    private LocalDateTime createdAt;
}
