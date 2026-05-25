package com.adarsh.financemanager.dto;

import com.adarsh.financemanager.entity.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Projection DTO used in JPQL aggregation queries for reports and goal progress.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummary {

    private String categoryName;
    private CategoryType categoryType;
    private BigDecimal totalAmount;
}
