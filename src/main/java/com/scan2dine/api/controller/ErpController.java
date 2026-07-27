package com.scan2dine.api.controller;

import com.scan2dine.api.dto.request.ErpConfigRequest;
import com.scan2dine.api.dto.request.ErpSyncRequest;
import com.scan2dine.api.dto.response.ApiResponse;
import com.scan2dine.api.dto.response.ErpStatusResponse;
import com.scan2dine.api.dto.response.StudentResponse;
import com.scan2dine.api.integration.erp.ErpIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/erp")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "ERP Integration", description = "Endpoints for College ERP synchronization (Campus7, Fedena, Academia, CAMU)")
public class ErpController {

    private final ErpIntegrationService erpIntegrationService;

    public ErpController(ErpIntegrationService erpIntegrationService) {
        this.erpIntegrationService = erpIntegrationService;
    }

    @PostMapping("/configure")
    @Operation(summary = "Configure the ERP endpoint credentials for the college tenant")
    public ResponseEntity<ApiResponse<Void>> configureErp(@Valid @RequestBody ErpConfigRequest request) {
        erpIntegrationService.configureErp(request);
        return ResponseEntity.ok(ApiResponse.success("ERP configured successfully."));
    }

    @GetMapping("/status")
    @Operation(summary = "Check ERP connection status and active parameters")
    public ResponseEntity<ApiResponse<ErpStatusResponse>> getErpStatus() {
        ErpStatusResponse response = erpIntegrationService.getErpStatus();
        return ResponseEntity.ok(ApiResponse.success("ERP status retrieved successfully.", response));
    }

    @PostMapping("/sync")
    @Operation(summary = "Synchronize a student record from the college ERP using their Roll Number")
    public ResponseEntity<ApiResponse<StudentResponse>> syncStudent(@Valid @RequestBody ErpSyncRequest request) {
        StudentResponse response = erpIntegrationService.syncStudent(request.getRollNumber());
        return ResponseEntity.ok(ApiResponse.success("Student synchronized from ERP successfully.", response));
    }
}
