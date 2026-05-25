package com.adarsh.financemanager.dto;

import com.adarsh.financemanager.entity.CategoryType;
import com.adarsh.financemanager.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String description;
    private String category;   // category name
    private CategoryType type; // INCOME or EXPENSE

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getDescription(),
                transaction.getCategory().getName(),
                transaction.getCategory().getType()
        );
    }
}
