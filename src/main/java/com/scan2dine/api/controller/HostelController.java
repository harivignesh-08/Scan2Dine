package com.scan2dine.api.controller;

import com.scan2dine.api.dto.request.HostelRequest;
import com.scan2dine.api.dto.response.ApiResponse;
import com.scan2dine.api.dto.response.HostelResponse;
import com.scan2dine.api.service.HostelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hostels")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Hostel Management", description = "Endpoints for managing hostel blocks")
public class HostelController {

    private final HostelService hostelService;

    public HostelController(HostelService hostelService) {
        this.hostelService = hostelService;
    }

    @PostMapping
    @Operation(summary = "Create a new Hostel block")
    public ResponseEntity<ApiResponse<HostelResponse>> createHostel(@Valid @RequestBody HostelRequest request) {
        HostelResponse response = hostelService.createHostel(request);
        return ResponseEntity.ok(ApiResponse.success("Hostel created successfully.", response));
    }

    @GetMapping
    @Operation(summary = "List all Hostel blocks registered under the college tenant")
    public ResponseEntity<ApiResponse<List<HostelResponse>>> getAllHostels() {
        List<HostelResponse> response = hostelService.getAllHostels();
        return ResponseEntity.ok(ApiResponse.success("Hostels retrieved successfully.", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a Hostel block details by ID")
    public ResponseEntity<ApiResponse<HostelResponse>> getHostelById(@PathVariable Long id) {
        HostelResponse response = hostelService.getHostelById(id);
        return ResponseEntity.ok(ApiResponse.success("Hostel retrieved successfully.", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a Hostel block details")
    public ResponseEntity<ApiResponse<HostelResponse>> updateHostel(@PathVariable Long id, @Valid @RequestBody HostelRequest request) {
        HostelResponse response = hostelService.updateHostel(id, request);
        return ResponseEntity.ok(ApiResponse.success("Hostel updated successfully.", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Hostel block")
    public ResponseEntity<ApiResponse<Void>> deleteHostel(@PathVariable Long id) {
        hostelService.deleteHostel(id);
        return ResponseEntity.ok(ApiResponse.success("Hostel deleted successfully."));
    }
}
