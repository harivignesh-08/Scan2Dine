package com.scan2dine.api.controller;

import com.scan2dine.api.dto.request.UserRequest;
import com.scan2dine.api.dto.response.ApiResponse;
import com.scan2dine.api.dto.response.UserResponse;
import com.scan2dine.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management (College Admin)", description = "Endpoints for College Admins to manage college staff accounts (Wardens, Assistant Admins)")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Register a new staff/warden account")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success("User account created successfully.", response));
    }

    @GetMapping
    @Operation(summary = "List all staff accounts registered under the college tenant")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully.", response));
    }

    @GetMapping("/wardens")
    @Operation(summary = "List all Warden accounts registered under the college tenant")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getWardens() {
        List<UserResponse> response = userService.getWardensByCollege();
        return ResponseEntity.ok(ApiResponse.success("Wardens retrieved successfully.", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user account details by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully.", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user account credentials or settings")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully.", response));
    }

    @PutMapping("/{id}/toggle")
    @Operation(summary = "Toggle user account active status (Suspend/Unsuspend warden access)")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(@PathVariable Long id) {
        UserResponse response = userService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("User status toggled successfully.", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Permanently delete a user account")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully."));
    }
}
