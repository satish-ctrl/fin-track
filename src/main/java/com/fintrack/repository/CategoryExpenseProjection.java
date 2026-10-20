
package com.fintrack.repository;

import java.math.BigDecimal;

public interface CategoryExpenseProjection {

    Long getCategoryId();

    String getCategoryName();

    BigDecimal getTotalExpense();
}
