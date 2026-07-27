package com.scan2dine.api.service.impl;

import com.scan2dine.api.dto.request.MealRequest;
import com.scan2dine.api.dto.response.MealResponse;
import com.scan2dine.api.entity.Meal;
import com.scan2dine.api.exception.BadRequestException;
import com.scan2dine.api.exception.ResourceNotFoundException;
import com.scan2dine.api.mapper.MealMapper;
import com.scan2dine.api.repository.MealRepository;
import com.scan2dine.api.service.MealService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MealServiceImpl implements MealService {

    private final MealRepository mealRepository;
    private final MealMapper mealMapper;

    public MealServiceImpl(MealRepository mealRepository, MealMapper mealMapper) {
        this.mealRepository = mealRepository;
        this.mealMapper = mealMapper;
    }

    @Override
    @Transactional
    public MealResponse createMeal(MealRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        if (mealRepository.findByMealName(request.getMealName()).isPresent()) {
            throw new BadRequestException("Meal settings for " + request.getMealName() + " already exist.");
        }

        Meal meal = new Meal();
        mealMapper.updateEntity(request, meal);

        Meal saved = mealRepository.save(meal);
        return mealMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealResponse> getAllMeals() {
        return mealRepository.findAll().stream()
                .map(mealMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MealResponse getMealById(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal settings not found with id " + id));
        return mealMapper.toResponse(meal);
    }

    @Override
    @Transactional
    public MealResponse updateMeal(Long id, MealRequest request) {
        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal settings not found with id " + id));

        if (!meal.getMealName().equalsIgnoreCase(request.getMealName()) && 
                mealRepository.findByMealName(request.getMealName()).isPresent()) {
            throw new BadRequestException("Meal settings for " + request.getMealName() + " already exist.");
        }

        mealMapper.updateEntity(request, meal);
        return mealMapper.toResponse(mealRepository.save(meal));
    }

    @Override
    @Transactional
    public void deleteMeal(Long id) {
        if (!mealRepository.existsById(id)) {
            throw new ResourceNotFoundException("Meal settings not found with id " + id);
        }
        mealRepository.deleteById(id);
    }
}
