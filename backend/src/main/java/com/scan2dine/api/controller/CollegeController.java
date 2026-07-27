package com.scan2dine.api.controller;

import com.scan2dine.api.dto.request.CollegeRequest;
import com.scan2dine.api.dto.request.RegisterRequest;
import com.scan2dine.api.dto.response.ApiResponse;
import com.scan2dine.api.dto.response.CollegeResponse;
import com.scan2dine.api.service.CollegeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colleges")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "College (Super Admin)", description = "College Tenant administration endpoints. Accessible only by SUPER_ADMIN.")
public class CollegeController {

    private final CollegeService collegeService;

    public CollegeController(CollegeService collegeService) {
        this.collegeService = collegeService;
    }

    @PostMapping
    @Operation(summary = "Create/Register a new College Tenant")
    public ResponseEntity<ApiResponse<CollegeResponse>> createCollege(@Valid @RequestBody RegisterRequest request) {
        CollegeResponse response = collegeService.registerCollege(request);
        return ResponseEntity.ok(ApiResponse.success("College registered successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get list of all registered College Tenants")
    public ResponseEntity<ApiResponse<List<CollegeResponse>>> getAllColleges() {
        List<CollegeResponse> response = collegeService.getAllColleges();
        return ResponseEntity.ok(ApiResponse.success("Colleges retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a College Tenant details by database ID")
    public ResponseEntity<ApiResponse<CollegeResponse>> getCollegeById(@PathVariable Long id) {
        CollegeResponse response = collegeService.getCollegeById(id);
        return ResponseEntity.ok(ApiResponse.success("College retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update College configuration profile (Logo, Theme Color, ERP bindings)")
    public ResponseEntity<ApiResponse<CollegeResponse>> updateCollege(@PathVariable Long id, @Valid @RequestBody CollegeRequest request) {
        CollegeResponse response = collegeService.updateCollege(id, request);
        return ResponseEntity.ok(ApiResponse.success("College updated successfully", response));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a College Tenant account, making it active")
    public ResponseEntity<ApiResponse<CollegeResponse>> approveCollege(@PathVariable Long id) {
        CollegeResponse response = collegeService.approveCollege(id);
        return ResponseEntity.ok(ApiResponse.success("College approved successfully", response));
    }

    @PutMapping("/{id}/suspend")
    @Operation(summary = "Suspend a College Tenant, blocking all its wardens/admins from logging in")
    public ResponseEntity<ApiResponse<CollegeResponse>> suspendCollege(@PathVariable Long id) {
        CollegeResponse response = collegeService.suspendCollege(id);
        return ResponseEntity.ok(ApiResponse.success("College suspended successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Permanently delete a College Tenant database record")
    public ResponseEntity<ApiResponse<Void>> deleteCollege(@PathVariable Long id) {
        collegeService.deleteCollege(id);
        return ResponseEntity.ok(ApiResponse.success("College deleted successfully"));
    }
}
