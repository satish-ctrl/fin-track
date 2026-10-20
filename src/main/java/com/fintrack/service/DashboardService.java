
package com.fintrack.service;

import com.fintrack.dto.dashboard.DashboardResponse;
import com.fintrack.dto.dashboard.CategoryExpenseResponse;
import com.fintrack.dto.dashboard.DailySummaryResponse;
import com.fintrack.entity.Transaction;
import com.fintrack.enums.AccountStatus;
import com.fintrack.enums.TransactionType;
import com.fintrack.repository.AccountRepository;
import com.fintrack.repository.CategoryExpenseProjection;
import com.fintrack.repository.DailySummaryProjection;
import com.fintrack.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public DashboardService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(
            String email,
            LocalDate fromDate,
            LocalDate toDate) {

        if (fromDate == null || toDate == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Both fromDate and toDate are required"
            );
        }

        if (fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "fromDate cannot be after toDate"
            );
        }

        BigDecimal totalIncome =
                transactionRepository.sumAmountByTypeAndDateRange(
                        email, TransactionType.INCOME, fromDate, toDate);

        BigDecimal totalExpenses =
                transactionRepository.sumAmountByTypeAndDateRange(
                        email, TransactionType.EXPENSE, fromDate, toDate);

        BigDecimal netCashFlow = totalIncome.subtract(totalExpenses);

        BigDecimal totalBalance =
                accountRepository.sumBalanceByUserEmailAndStatus(
                        email, AccountStatus.ACTIVE);

        List<CategoryExpenseProjection> categoryResults =
                transactionRepository.findCategoryWiseExpenses(
                        email, TransactionType.EXPENSE, fromDate, toDate);

        List<CategoryExpenseResponse> categoryWiseExpenses =
                categoryResults.stream()
                        .map(c -> new CategoryExpenseResponse(
                                c.getCategoryId(),
                                c.getCategoryName(),
                                c.getTotalExpense()))
                        .toList();

        // Daily summary will be implemented in the next step.
        List<DailySummaryProjection> dailyResults =
                transactionRepository.findDailySummary(
                        email,
                        fromDate,
                        toDate,
                        TransactionType.INCOME,
                        TransactionType.EXPENSE
                );

        List<DailySummaryResponse> dailySummary =
                dailyResults.stream()
                        .map(d -> new DailySummaryResponse(
                                d.getDate(),
                                d.getIncome(),
                                d.getExpense()
                        ))
                        .toList();

        return new DashboardResponse(
                totalIncome,
                totalExpenses,
                netCashFlow,
                totalBalance,
                categoryWiseExpenses,
                dailySummary
        );
    }
}
