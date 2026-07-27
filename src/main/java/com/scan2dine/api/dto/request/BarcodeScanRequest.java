package com.scan2dine.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BarcodeScanRequest {

    @NotBlank(message = "Barcode value is required")
    private String barcodeValue;
}
