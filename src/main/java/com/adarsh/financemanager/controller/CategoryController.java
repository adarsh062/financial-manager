package com.adarsh.financemanager.controller;

import com.adarsh.financemanager.dto.CategoryRequest;
import com.adarsh.financemanager.dto.CategoryResponse;
import com.adarsh.financemanager.entity.User;
import com.adarsh.financemanager.security.SecurityUtils;
import com.adarsh.financemanager.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<Map<String, List<CategoryResponse>>> getCategories() {
        User user = securityUtils.getCurrentUser();
        List<CategoryResponse> categories = categoryService.getCategories(user);
        return ResponseEntity.ok(Map.of("categories", categories));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request
    ) {
        User user = securityUtils.getCurrentUser();
        return new ResponseEntity<>(
                categoryService.createCategory(request, user),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, String>> deleteCategory(
            @PathVariable String name
    ) {
        User user = securityUtils.getCurrentUser();
        categoryService.deleteCategory(name, user);
        return ResponseEntity.ok(Map.of("message", "Category deleted successfully"));
    }
}
