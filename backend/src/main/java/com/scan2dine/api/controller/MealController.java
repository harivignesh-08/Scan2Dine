package com.scan2dine.api.controller;

import com.scan2dine.api.dto.request.MealRequest;
import com.scan2dine.api.dto.response.ApiResponse;
import com.scan2dine.api.dto.response.MealResponse;
import com.scan2dine.api.service.MealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meals")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Meal Configuration", description = "Endpoints for configuring breakfast, lunch, and dinner timings")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @PostMapping
    @Operation(summary = "Configure a new Meal session timing (Breakfast, Lunch, Dinner)")
    public ResponseEntity<ApiResponse<MealResponse>> createMeal(@Valid @RequestBody MealRequest request) {
        MealResponse response = mealService.createMeal(request);
        return ResponseEntity.ok(ApiResponse.success("Meal settings configured successfully.", response));
    }

    @GetMapping
    @Operation(summary = "Get list of all configured Meal sessions for the college")
    public ResponseEntity<ApiResponse<List<MealResponse>>> getAllMeals() {
        List<MealResponse> response = mealService.getAllMeals();
        return ResponseEntity.ok(ApiResponse.success("Meal settings retrieved successfully.", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Meal session settings by ID")
    public ResponseEntity<ApiResponse<MealResponse>> getMealById(@PathVariable Long id) {
        MealResponse response = mealService.getMealById(id);
        return ResponseEntity.ok(ApiResponse.success("Meal settings retrieved successfully.", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Meal session timings")
    public ResponseEntity<ApiResponse<MealResponse>> updateMeal(@PathVariable Long id, @Valid @RequestBody MealRequest request) {
        MealResponse response = mealService.updateMeal(id, request);
        return ResponseEntity.ok(ApiResponse.success("Meal settings updated successfully.", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a configured Meal session")
    public ResponseEntity<ApiResponse<Void>> deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);
        return ResponseEntity.ok(ApiResponse.success("Meal settings deleted successfully."));
    }
}
