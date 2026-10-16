package com.fintrack.dto;

import com.fintrack.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountCreateRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 50, message = "Account name cannot exceed 50 characters")
    private String name;

    @NotNull(message = "Account type is required")
    private AccountType type;
}
