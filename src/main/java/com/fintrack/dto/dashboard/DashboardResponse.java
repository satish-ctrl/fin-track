
package com.fintrack.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netCashFlow,
        BigDecimal totalBalance,
        List<CategoryExpenseResponse> categoryWiseExpenses,
        List<DailySummaryResponse> dailySummary
) {
}
