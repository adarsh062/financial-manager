package com.adarsh.financemanager.service.impl;

import com.adarsh.financemanager.dto.CategoryRequest;
import com.adarsh.financemanager.dto.CategoryResponse;
import com.adarsh.financemanager.entity.Category;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.exception.CategoryInUseException;
import com.adarsh.financemanager.exception.ForbiddenAccessException;
import com.adarsh.financemanager.exception.ResourceAlreadyExistsException;
import com.adarsh.financemanager.exception.ResourceNotFoundException;
import com.adarsh.financemanager.repository.CategoryRepository;
import com.adarsh.financemanager.repository.TransactionRepository;
import com.adarsh.financemanager.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public List<CategoryResponse> getCategories(User user) {
        return categoryRepository.findAllVisibleForUser(user)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, User user) {
        // Enforce uniqueness per user against custom and default categories
        if (categoryRepository.existsByNameAndUserAndIsDeletedFalse(request.getName(), user) ||
            categoryRepository.existsByNameAndIsCustomFalse(request.getName())) {
            throw new ResourceAlreadyExistsException(
                    "Category '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .isCustom(true)
                .isDeleted(false)
                .user(user)
                .build();

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(String name, User user) {
        // Try to find custom category first, then fallback to visible search to ensure 403 on default categories
        Category category = categoryRepository.findByNameAndUser(name, user)
                .or(() -> categoryRepository.findVisibleByNameAndUser(name, user))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category '" + name + "' not found"));

        // Prevent deletion of default categories
        if (!category.isCustom()) {
            throw new ForbiddenAccessException("Default categories cannot be deleted");
        }

        // Prevent deletion if category is used in any transaction
        if (transactionRepository.existsByCategoryAndUser(category, user)) {
            throw new CategoryInUseException(
                    "Category '" + name + "' is used in existing transactions and cannot be deleted");
        }

        // Soft-delete
        category.setDeleted(true);
        categoryRepository.save(category);
    }
}
