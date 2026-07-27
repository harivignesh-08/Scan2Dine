package com.scan2dine.api.controller;

import com.scan2dine.api.dto.request.RoomRequest;
import com.scan2dine.api.dto.response.ApiResponse;
import com.scan2dine.api.dto.response.RoomResponse;
import com.scan2dine.api.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Room Management", description = "Endpoints for managing rooms within hostel blocks")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @Operation(summary = "Create/Add a new Room to a Hostel block")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody RoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.ok(ApiResponse.success("Room created successfully.", response));
    }

    @GetMapping
    @Operation(summary = "List all Rooms registered under the college tenant")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        List<RoomResponse> response = roomService.getAllRooms();
        return ResponseEntity.ok(ApiResponse.success("Rooms retrieved successfully.", response));
    }

    @GetMapping("/hostel/{hostelId}")
    @Operation(summary = "List all Rooms belonging to a specific Hostel block")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomsByHostel(@PathVariable Long hostelId) {
        List<RoomResponse> response = roomService.getRoomsByHostel(hostelId);
        return ResponseEntity.ok(ApiResponse.success("Rooms retrieved successfully.", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Room details by database ID")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        RoomResponse response = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success("Room retrieved successfully.", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Room configurations (Number, capacity limits)")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        RoomResponse response = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully.", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Permanently delete a Room record")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully."));
    }
}
