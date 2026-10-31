package com.fintrack.dto;

public record ProfileResponse(
        Long id,
        String username,
        String email
) {
}