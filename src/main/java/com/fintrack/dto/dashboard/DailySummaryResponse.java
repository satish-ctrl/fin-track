
package com.fintrack.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySummaryResponse(
        LocalDate date,
        BigDecimal income,
        BigDecimal expense
) {
}
