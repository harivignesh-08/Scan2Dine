package com.scan2dine.api.mapper;

import com.scan2dine.api.dto.request.MealRequest;
import com.scan2dine.api.dto.response.MealResponse;
import com.scan2dine.api.entity.Meal;
import org.springframework.stereotype.Component;

@Component
public class MealMapper {

    public MealResponse toResponse(Meal meal) {
        if (meal == null) return null;
        MealResponse res = new MealResponse();
        res.setId(meal.getId());
        res.setMealName(meal.getMealName());
        res.setStartTime(meal.getStartTime());
        res.setEndTime(meal.getEndTime());
        return res;
    }

    public void updateEntity(MealRequest req, Meal meal) {
        if (req == null || meal == null) return;
        meal.setMealName(req.getMealName());
        meal.setStartTime(req.getStartTime());
        meal.setEndTime(req.getEndTime());
    }
}
