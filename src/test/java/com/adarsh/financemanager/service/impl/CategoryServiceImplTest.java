package com.adarsh.financemanager.service.impl;

import com.adarsh.financemanager.dto.CategoryRequest;
import com.adarsh.financemanager.dto.CategoryResponse;
import com.adarsh.financemanager.entity.Category;
import com.adarsh.financemanager.entity.CategoryType;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.exception.CategoryInUseException;
import com.adarsh.financemanager.exception.ForbiddenAccessException;
import com.adarsh.financemanager.exception.ResourceAlreadyExistsException;
import com.adarsh.financemanager.exception.ResourceNotFoundException;
import com.adarsh.financemanager.repository.CategoryRepository;
import com.adarsh.financemanager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl Tests")
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks private CategoryServiceImpl categoryService;

    private User user;
    private Category customCategory;
    private Category defaultCategory;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();

        customCategory = Category.builder()
                .id(10L).name("Travel").type(CategoryType.EXPENSE)
                .isCustom(true).isDeleted(false).user(user).build();

        defaultCategory = Category.builder()
                .id(1L).name("Salary").type(CategoryType.INCOME)
                .isCustom(false).isDeleted(false).user(null).build();
    }

    // ── getCategories ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getCategories: returns visible categories for user")
    void getCategories_returnsListForUser() {
        when(categoryRepository.findAllVisibleForUser(user))
                .thenReturn(List.of(defaultCategory, customCategory));

        List<CategoryResponse> result = categoryService.getCategories(user);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Salary");
    }

    // ── createCategory ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createCategory: success — saves and returns response")
    void createCategory_success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Travel");
        request.setType(CategoryType.EXPENSE);

        when(categoryRepository.existsByNameAndUserAndIsDeletedFalse("Travel", user)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(customCategory);

        CategoryResponse response = categoryService.createCategory(request, user);

        assertThat(response.getName()).isEqualTo("Travel");
        assertThat(response.isCustom()).isTrue();
    }

    @Test
    @DisplayName("createCategory: duplicate name → throws ResourceAlreadyExistsException")
    void createCategory_duplicate_throws() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Travel");
        request.setType(CategoryType.EXPENSE);

        when(categoryRepository.existsByNameAndUserAndIsDeletedFalse("Travel", user)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request, user))
                .isInstanceOf(ResourceAlreadyExistsException.class);
        verify(categoryRepository, never()).save(any());
    }

    // ── deleteCategory ────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteCategory: success — soft-deletes custom category")
    void deleteCategory_success() {
        when(categoryRepository.findByNameAndUser("Travel", user))
                .thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryAndUser(customCategory, user)).thenReturn(false);

        categoryService.deleteCategory("Travel", user);

        assertThat(customCategory.isDeleted()).isTrue();
        verify(categoryRepository).save(customCategory);
    }

    @Test
    @DisplayName("deleteCategory: not found → throws ResourceNotFoundException")
    void deleteCategory_notFound_throws() {
        when(categoryRepository.findByNameAndUser("Unknown", user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory("Unknown", user))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteCategory: default category → throws ForbiddenAccessException")
    void deleteCategory_defaultCategory_throws() {
        when(categoryRepository.findByNameAndUser("Salary", user))
                .thenReturn(Optional.of(defaultCategory));

        assertThatThrownBy(() -> categoryService.deleteCategory("Salary", user))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("Default categories cannot be deleted");
    }

    @Test
    @DisplayName("deleteCategory: used in transactions → throws CategoryInUseException")
    void deleteCategory_usedInTransactions_throws() {
        when(categoryRepository.findByNameAndUser("Travel", user))
                .thenReturn(Optional.of(customCategory));
        when(transactionRepository.existsByCategoryAndUser(customCategory, user)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory("Travel", user))
                .isInstanceOf(CategoryInUseException.class);
    }
}
