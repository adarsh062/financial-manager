package com.adarsh.financemanager.dto;

import com.adarsh.financemanager.entity.Category;
import com.adarsh.financemanager.entity.CategoryType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CategoryResponse {

    private String name;
    private CategoryType type;
    
    @JsonProperty("custom")
    private boolean isCustom;

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getName(),
                category.getType(),
                category.isCustom()
        );
    }
}
