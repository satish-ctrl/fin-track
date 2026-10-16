
package com.fintrack.dto;

import com.fintrack.enums.AccountType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountUpdateRequest {

    @Size(max = 50, message = "Account name cannot exceed 50 characters")
    private String name;

    private AccountType type;

    public AccountUpdateRequest() {
    }
}
