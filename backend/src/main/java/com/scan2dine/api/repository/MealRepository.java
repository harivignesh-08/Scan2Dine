package com.scan2dine.api.repository;

import com.scan2dine.api.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MealRepository extends JpaRepository<Meal, Long> {
    Optional<Meal> findByMealName(String mealName);
}
