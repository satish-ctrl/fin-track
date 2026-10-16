package com.fintrack.dto;

import com.fintrack.enums.UserStatus;

public record RegisterResponse (
        Long id,
        String username,
        String email
){}
