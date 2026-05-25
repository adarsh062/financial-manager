package com.adarsh.financemanager.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Used for PUT /api/goals/{id}
 * Fields are optional to support partial updates.
 */
@Getter
@Setter
public class GoalUpdateRequest {

    private BigDecimal targetAmount;
    private LocalDate targetDate;
}
