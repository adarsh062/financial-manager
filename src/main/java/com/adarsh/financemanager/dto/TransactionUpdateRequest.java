package com.adarsh.financemanager.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Used for PUT /api/transactions/{id}
 * Fields are optional to support partial updates.
 */
@Getter
@Setter
public class TransactionUpdateRequest {

    private BigDecimal amount;
    private String description;
}
