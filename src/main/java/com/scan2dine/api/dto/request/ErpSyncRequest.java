package com.scan2dine.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErpSyncRequest {

    @NotBlank(message = "Roll number is required")
    private String rollNumber;
}
