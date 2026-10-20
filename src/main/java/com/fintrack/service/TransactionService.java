
package com.fintrack.service;

import com.fintrack.dto.TransactionCreateRequest;
import com.fintrack.dto.TransactionResponse;
import com.fintrack.entity.Account;
import com.fintrack.entity.Category;
import com.fintrack.entity.Transaction;
import com.fintrack.entity.User;
import com.fintrack.enums.AccountStatus;
import com.fintrack.enums.UserStatus;
import com.fintrack.repository.AccountRepository;
import com.fintrack.repository.CategoryRepository;
import com.fintrack.repository.TransactionRepository;
import com.fintrack.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {

        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransactionResponse createTransaction(
            String email,
            TransactionCreateRequest request) {

        // 1. Validate request before touching account balances.
        validateTransactionRequest(request);

        // 2. Verify that the authenticated user is active.
        User user = userRepository
                .findByEmailAndStatus(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Active user not found"
                ));

        // 3. Lock all required accounts in ascending ID order.
        Map<Long, Account> accounts =
                lockAccounts(email, request);

        // 4. Validate category ownership.
        Category category = null;

        if (request.getCategoryId() != null) {
            category = categoryRepository
                    .findByIdAndUser_Email(
                            request.getCategoryId(), email)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Category not found"
                    ));
        }

        Account fromAccount = request.getFromAccountId() == null
                ? null
                : accounts.get(request.getFromAccountId());

        Account toAccount = request.getToAccountId() == null
                ? null
                : accounts.get(request.getToAccountId());

        // 5. Verify account status and update balances.
        BigDecimal amount = request.getAmount();

        switch (request.getType()) {

            case INCOME -> {
                requireActiveAccount(toAccount);
                toAccount.setBalance(
                        toAccount.getBalance().add(amount));
            }

            case EXPENSE -> {
                requireActiveAccount(fromAccount);
                ensureSufficientBalance(fromAccount, amount);

                fromAccount.setBalance(
                        fromAccount.getBalance().subtract(amount));
            }

            case TRANSFER -> {
                requireActiveAccount(fromAccount);
                requireActiveAccount(toAccount);

                ensureSufficientBalance(fromAccount, amount);

                fromAccount.setBalance(
                        fromAccount.getBalance().subtract(amount));

                toAccount.setBalance(
                        toAccount.getBalance().add(amount));
            }
        }

        // 6. Create the immutable transaction history record.
        Transaction transaction = new Transaction();

        transaction.setType(request.getType());
        transaction.setAmount(amount);
        transaction.setUser(user);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setCategory(category);
        transaction.setTransactionDate(
                request.getTransactionDate());

        String description = request.getDescription();

        transaction.setDescription(
                description == null || description.isBlank()
                        ? null
                        : description.trim()
        );

        Transaction saved =
                transactionRepository.save(transaction);

        return TransactionResponse.from(saved);
    }

    private void validateTransactionRequest(
            TransactionCreateRequest request) {

        if (request == null) {
            throw badRequest("Request body is required");
        }

        if (request.getType() == null) {
            throw badRequest("Transaction type is required");
        }

        if (request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw badRequest("Amount must be greater than zero");
        }

        if (request.getTransactionDate() == null) {
            throw badRequest("Transaction date is required");
        }

        Long fromId = request.getFromAccountId();
        Long toId = request.getToAccountId();
        Long categoryId = request.getCategoryId();

        switch (request.getType()) {

            case INCOME -> {
                if (toId == null || fromId != null) {
                    throw badRequest(
                            "INCOME requires only toAccountId");
                }

                if (categoryId == null) {
                    throw badRequest(
                            "Category is required for INCOME");
                }
            }

            case EXPENSE -> {
                if (fromId == null || toId != null) {
                    throw badRequest(
                            "EXPENSE requires only fromAccountId");
                }

                if (categoryId == null) {
                    throw badRequest(
                            "Category is required for EXPENSE");
                }
            }

            case TRANSFER -> {
                if (fromId == null || toId == null) {
                    throw badRequest(
                            "Both account IDs are required for TRANSFER");
                }

                if (fromId.equals(toId)) {
                    throw badRequest(
                            "Source and destination accounts must be different");
                }

                if (categoryId != null) {
                    throw badRequest(
                            "Category is not allowed for TRANSFER");
                }
            }
        }
    }

    private Map<Long, Account> lockAccounts(
            String email,
            TransactionCreateRequest request) {

        TreeSet<Long> accountIds = new TreeSet<>();

        if (request.getFromAccountId() != null) {
            accountIds.add(request.getFromAccountId());
        }

        if (request.getToAccountId() != null) {
            accountIds.add(request.getToAccountId());
        }

        Map<Long, Account> accounts = new HashMap<>();

        for (Long accountId : accountIds) {
            Account account = accountRepository
                    .findByIdAndUserEmailForUpdate(accountId, email)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Account not found"
                    ));

            accounts.put(accountId, account);
        }

        return accounts;
    }

    private void requireActiveAccount(Account account) {
        if (account == null) {
            throw badRequest("Required account was not provided");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw badRequest("Account is not active");
        }
    }

    private void ensureSufficientBalance(
            Account account,
            BigDecimal amount) {

        if (account.getBalance().compareTo(amount) < 0) {
            throw badRequest("Insufficient account balance");
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }


//    Transaction History

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionHistory(
            String email,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size) {

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "fromDate cannot be after toDate"
            );
        }

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100"
            );
        }

        Pageable pageable = PageRequest.of(page, size);

        return transactionRepository.findTransactionHistory(
                email,
                fromDate,
                toDate,
                pageable
        ).map(TransactionResponse::from);
    }

}
