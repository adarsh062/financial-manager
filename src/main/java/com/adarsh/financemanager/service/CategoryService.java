package com.adarsh.financemanager.service;

import com.adarsh.financemanager.dto.CategoryRequest;
import com.adarsh.financemanager.dto.CategoryResponse;
import com.adarsh.financemanager.entity.User;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getCategories(User user);

    CategoryResponse createCategory(CategoryRequest request, User user);

    void deleteCategory(String name, User user);
}
