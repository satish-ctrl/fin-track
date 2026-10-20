package com.fintrack.dto;

import com.fintrack.entity.Transaction;
import com.fintrack.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TransactionResponse {

    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private Long fromAccountId;
    private Long toAccountId;
    private Long categoryId;
    private LocalDate transactionDate;
    private String description;

    public static TransactionResponse from(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();

        response.setId(transaction.getId());
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());

        response.setFromAccountId(
                transaction.getFromAccount() != null
                        ? transaction.getFromAccount().getId()
                        : null
        );

        response.setToAccountId(
                transaction.getToAccount() != null
                        ? transaction.getToAccount().getId()
                        : null
        );

        response.setCategoryId(
                transaction.getCategory() != null
                        ? transaction.getCategory().getId()
                        : null
        );

        response.setTransactionDate(transaction.getTransactionDate());
        response.setDescription(transaction.getDescription());

        return response;
    }
}