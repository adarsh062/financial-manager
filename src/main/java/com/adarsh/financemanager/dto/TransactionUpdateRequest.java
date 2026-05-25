package com.adarsh.financemanager.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Used for PUT /api/transactions/{id}
 * Only amount and description can be updated.
 * Date and category are immutable after creation.
 */
@Getter
@Setter
public class TransactionUpdateRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String description;
}
