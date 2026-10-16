package com.fintrack.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
) {

}
