package com.scan2dine.api.controller;

import com.scan2dine.api.dto.request.BarcodeRegistrationRequest;
import com.scan2dine.api.dto.request.BarcodeScanRequest;
import com.scan2dine.api.dto.response.ApiResponse;
import com.scan2dine.api.dto.response.BarcodeScanResponse;
import com.scan2dine.api.service.BarcodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/barcode")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Barcode Scanner Operations", description = "Endpoints for registering student ID cards and real-time scanning validation")
public class BarcodeController {

    private final BarcodeService barcodeService;

    public BarcodeController(BarcodeService barcodeService) {
        this.barcodeService = barcodeService;
    }

    @PostMapping("/register")
    @Operation(summary = "Map/Register an existing College ID barcode value to a student")
    public ResponseEntity<ApiResponse<Void>> registerBarcode(@Valid @RequestBody BarcodeRegistrationRequest request) {
        barcodeService.registerBarcode(request);
        return ResponseEntity.ok(ApiResponse.success("Barcode registered to student successfully."));
    }

    @PostMapping("/scan")
    @Operation(summary = "Scan a student's ID card barcode (Warden mobile client scan simulation)")
    public ResponseEntity<ApiResponse<BarcodeScanResponse>> scanBarcode(@Valid @RequestBody BarcodeScanRequest request) {
        BarcodeScanResponse response = barcodeService.scanBarcode(request);
        
        if ("ACCESS_GRANTED".equalsIgnoreCase(response.getStatus())) {
            return ResponseEntity.ok(ApiResponse.success("Access Granted.", response));
        } else {
            return ResponseEntity.ok(ApiResponse.success("Access Denied.", response));
        }
    }
}
