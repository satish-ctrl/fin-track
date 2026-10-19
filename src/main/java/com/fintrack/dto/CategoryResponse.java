package com.fintrack.dto;

import com.fintrack.entity.Category;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CategoryResponse {

    private Long id;
    private String name;
    private Instant createdAt;

    public static CategoryResponse from(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setCreatedAt(category.getCreated().toInstant());
        return response;
    }
}