package com.scan2dine.api.service;

import com.scan2dine.api.dto.request.MealRequest;
import com.scan2dine.api.dto.response.MealResponse;

import java.util.List;

public interface MealService {
    MealResponse createMeal(MealRequest request);
    List<MealResponse> getAllMeals();
    MealResponse getMealById(Long id);
    MealResponse updateMeal(Long id, MealRequest request);
    void deleteMeal(Long id);
}
