package com.adarsh.financemanager.dto;

import com.adarsh.financemanager.entity.Category;
import com.adarsh.financemanager.entity.CategoryType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryResponse {

    private String name;
    private CategoryType type;
    
    @JsonProperty("custom")
    private boolean custom;

    @JsonProperty("isCustom")
    private boolean isCustom;

    public CategoryResponse(String name, CategoryType type, boolean isCustom) {
        this.name = name;
        this.type = type;
        this.isCustom = isCustom;
        this.custom = isCustom;
    }

    public static CategoryResponse from(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponse(
                category.getName(),
                category.getType(),
                category.isCustom()
        );
    }
}
