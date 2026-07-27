package com.scan2dine.api.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class MealResponse {
    private Long id;
    private String mealName;
    private LocalTime startTime;
    private LocalTime endTime;
}
