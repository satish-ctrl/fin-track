
package com.fintrack.repository;

import com.fintrack.entity.Transaction;
import com.fintrack.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    boolean existsByFromAccount_IdOrToAccount_Id(
            Long fromAccountId,
            Long toAccountId
    );

    @Query(
            value = """
        SELECT t
        FROM Transaction t
        WHERE t.user.email = :email
          AND (
              CAST(:fromDate AS date) IS NULL
              OR t.transactionDate >= :fromDate
          )
          AND (
              CAST(:toDate AS date) IS NULL
              OR t.transactionDate <= :toDate
          )
        ORDER BY t.transactionDate DESC, t.id DESC
        """,
            countQuery = """
        SELECT COUNT(t)
        FROM Transaction t
        WHERE t.user.email = :email
          AND (
              CAST(:fromDate AS date) IS NULL
              OR t.transactionDate >= :fromDate
          )
          AND (
              CAST(:toDate AS date) IS NULL
              OR t.transactionDate <= :toDate
          )
        """
    )
    Page<Transaction> findTransactionHistory(
            @Param("email") String email,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );


    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.user.email = :email
      AND t.type = :type
      AND t.transactionDate >= :fromDate
      AND t.transactionDate <= :toDate
    """)
    BigDecimal sumAmountByTypeAndDateRange(
            @Param("email") String email,
            @Param("type") TransactionType type,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );


    @Query("""
    SELECT
        c.id AS categoryId,
        c.name AS categoryName,
        SUM(t.amount) AS totalExpense
    FROM Transaction t
    JOIN t.category c
    WHERE t.user.email = :email
      AND t.type = :type
      AND t.transactionDate >= :fromDate
      AND t.transactionDate <= :toDate
    GROUP BY c.id, c.name
    ORDER BY SUM(t.amount) DESC
    """)
    List<CategoryExpenseProjection> findCategoryWiseExpenses(
            @Param("email") String email,
            @Param("type") TransactionType type,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );


    @Query("""
    SELECT
        t.transactionDate AS date,
        SUM(
            CASE
                WHEN t.type = :incomeType THEN t.amount
                ELSE 0
            END
        ) AS income,
        SUM(
            CASE
                WHEN t.type = :expenseType THEN t.amount
                ELSE 0
            END
        ) AS expense
    FROM Transaction t
    WHERE t.user.email = :email
      AND t.transactionDate >= :fromDate
      AND t.transactionDate <= :toDate
      AND t.type IN (:incomeType, :expenseType)
    GROUP BY t.transactionDate
    ORDER BY t.transactionDate ASC
    """)
    List<DailySummaryProjection> findDailySummary(
            @Param("email") String email,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("incomeType") TransactionType incomeType,
            @Param("expenseType") TransactionType expenseType
    );


}
