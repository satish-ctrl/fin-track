package com.fintrack.controller;

import com.fintrack.dto.CategoryCreateRequest;
import com.fintrack.dto.CategoryResponse;
import com.fintrack.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryCreateRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        CategoryResponse response =
                categoryService.createCategory(email, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(
            Authentication authentication) {

        String email = authentication.getName();

        List<CategoryResponse> categories =
                categoryService.getCategories(email);

        return ResponseEntity.ok(categories);
    }
}