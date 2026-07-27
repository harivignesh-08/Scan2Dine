package com.scan2dine.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ErpStatusResponse {
    private boolean configured;
    private String erpName;
    private String erpBaseUrl;
    private String status; // CONNECTED, ERROR, NOT_CONFIGURED
    private String message;
}
