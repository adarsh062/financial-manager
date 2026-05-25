package com.adarsh.financemanager.controller;

import com.adarsh.financemanager.dto.CategoryRequest;
import com.adarsh.financemanager.dto.CategoryResponse;
import com.adarsh.financemanager.entity.CategoryType;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.exception.CategoryInUseException;
import com.adarsh.financemanager.exception.ForbiddenAccessException;
import com.adarsh.financemanager.exception.GlobalExceptionHandler;
import com.adarsh.financemanager.exception.ResourceAlreadyExistsException;
import com.adarsh.financemanager.exception.ResourceNotFoundException;
import com.adarsh.financemanager.security.SecurityConfig;
import com.adarsh.financemanager.security.SecurityUtils;
import com.adarsh.financemanager.service.CategoryService;
import com.adarsh.financemanager.service.impl.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("CategoryController Tests")
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CategoryService categoryService;
    @MockBean private SecurityUtils securityUtils;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        when(securityUtils.getCurrentUser()).thenReturn(user);
    }

    // ── GET /api/categories ───────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("GET /api/categories: returns list → 200")
    void getCategories_returns200() throws Exception {
        CategoryResponse salary = new CategoryResponse("Salary", CategoryType.INCOME, false);
        CategoryResponse travel = new CategoryResponse("Travel", CategoryType.EXPENSE, true);

        when(categoryService.getCategories(user)).thenReturn(List.of(salary, travel));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories.length()").value(2))
                .andExpect(jsonPath("$.categories[0].name").value("Salary"))
                .andExpect(jsonPath("$.categories[1].name").value("Travel"))
                .andExpect(jsonPath("$.categories[1].isCustom").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/categories: unauthenticated → 401")
    void getCategories_noAuth_returns401() throws Exception {
        when(securityUtils.getCurrentUser())
                .thenThrow(new ResourceNotFoundException("Authenticated user not found"));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/categories ──────────────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("POST /api/categories: success → 201")
    void createCategory_success_returns201() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("Travel");
        req.setType(CategoryType.EXPENSE);

        CategoryResponse resp = new CategoryResponse("Travel", CategoryType.EXPENSE, true);
        when(categoryService.createCategory(any(), eq(user))).thenReturn(resp);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Travel"))
                .andExpect(jsonPath("$.isCustom").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/categories: duplicate name → 409")
    void createCategory_duplicate_returns409() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setName("Travel");
        req.setType(CategoryType.EXPENSE);

        when(categoryService.createCategory(any(), eq(user)))
                .thenThrow(new ResourceAlreadyExistsException("Category 'Travel' already exists"));

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Category 'Travel' already exists"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/categories: missing name → 400")
    void createCategory_missingName_returns400() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.setType(CategoryType.EXPENSE);

        mockMvc.perform(post("/api/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /api/categories/{name} ─────────────────────────────────────────

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/categories/{name}: success → 200")
    void deleteCategory_success_returns200() throws Exception {
        doNothing().when(categoryService).deleteCategory(eq("Travel"), eq(user));

        mockMvc.perform(delete("/api/categories/Travel")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/categories/{name}: default category → 403")
    void deleteCategory_default_returns403() throws Exception {
        doThrow(new ForbiddenAccessException("Default categories cannot be deleted"))
                .when(categoryService).deleteCategory(eq("Salary"), eq(user));

        mockMvc.perform(delete("/api/categories/Salary")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Default categories cannot be deleted"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/categories/{name}: in-use category → 400")
    void deleteCategory_inUse_returns400() throws Exception {
        doThrow(new CategoryInUseException("Category 'Travel' is used in existing transactions"))
                .when(categoryService).deleteCategory(eq("Travel"), eq(user));

        mockMvc.perform(delete("/api/categories/Travel")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Category 'Travel' is used in existing transactions"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/categories/{name}: not found → 404")
    void deleteCategory_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Category 'Unknown' not found"))
                .when(categoryService).deleteCategory(eq("Unknown"), eq(user));

        mockMvc.perform(delete("/api/categories/Unknown")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Category 'Unknown' not found"));
    }
}
