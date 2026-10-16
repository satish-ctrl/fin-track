package com.fintrack.dto;

import com.fintrack.enums.AccountStatus;
import com.fintrack.enums.AccountType;

import java.math.BigDecimal;

public record AccountResponse(Long id,
                              String name,
                              AccountType type,
                              BigDecimal balance,
                              AccountStatus status) {
}
