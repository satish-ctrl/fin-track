package com.fintrack.service;

import com.fintrack.dto.CategoryCreateRequest;
import com.fintrack.dto.CategoryResponse;
import com.fintrack.entity.Category;
import com.fintrack.entity.User;
import com.fintrack.enums.UserStatus;
import com.fintrack.repository.CategoryRepository;
import com.fintrack.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CategoryResponse createCategory(
            String email,
            CategoryCreateRequest request) {

        String name = request.getName().trim();

        if (name.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Category name is required"
            );
        }

        if (categoryRepository
                .existsByUser_EmailAndNameIgnoreCase(email, name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category already exists"
            );
        }

        User user = userRepository
                .findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Active user not found"
                ));

        Category category = new Category();
        category.setName(name);
        category.setUser(user);

        Category savedCategory = categoryRepository.save(category);

        return CategoryResponse.from(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(String email) {

        return categoryRepository
                .findAllByUser_EmailOrderByNameAsc(email)
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }
}