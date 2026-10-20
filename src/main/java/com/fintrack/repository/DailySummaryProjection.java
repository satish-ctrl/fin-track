
package com.fintrack.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailySummaryProjection {

    LocalDate getDate();

    BigDecimal getIncome();

    BigDecimal getExpense();
}
