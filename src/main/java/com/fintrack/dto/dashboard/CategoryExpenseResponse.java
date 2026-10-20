
package com.fintrack.dto.dashboard;

import java.math.BigDecimal;

public record CategoryExpenseResponse(
        Long categoryId,
        String categoryName,
        BigDecimal totalExpense
) {
}
