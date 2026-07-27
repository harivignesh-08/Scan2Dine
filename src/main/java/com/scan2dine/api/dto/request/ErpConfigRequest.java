package com.scan2dine.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErpConfigRequest {

    @NotBlank(message = "ERP Name is required (e.g. Campus7, Fedena, Academia, CAMU, Custom ERP)")
    private String erpName;

    @NotBlank(message = "ERP Base URL is required")
    private String erpBaseUrl;

    @NotBlank(message = "ERP API Key is required")
    private String erpApiKey;
}
