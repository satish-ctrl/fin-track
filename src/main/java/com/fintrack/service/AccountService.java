package com.fintrack.service;

import com.fintrack.dto.AccountCreateRequest;
import com.fintrack.dto.AccountResponse;
import com.fintrack.dto.AccountUpdateRequest;
import com.fintrack.entity.Account;
import com.fintrack.entity.User;
import com.fintrack.enums.AccountStatus;
import com.fintrack.enums.UserStatus;
import com.fintrack.repository.AccountRepository;
import com.fintrack.repository.TransactionRepository;
import com.fintrack.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private static final String ACCOUNT_NOT_FOUND = "Account not found";

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    public AccountService(
            AccountRepository accountRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            CurrentUserService currentUserService
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    // Create a new account for the authenticated user
    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {

        String email = currentUserService.getCurrentUserEmail();

        User user = userRepository.findByEmailAndStatus(
                email,
                UserStatus.ACTIVE
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user not found"
        ));

        Account account = new Account();
        account.setName(request.getName().trim());
        account.setType(request.getType());
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);
        account.setUser(user);

        Account savedAccount = accountRepository.save(account);

        return toAccountResponse(savedAccount);
    }

    // Get all active accounts belonging to the authenticated user
    public List<AccountResponse> getAllMyAccounts() {

        String email = currentUserService.getCurrentUserEmail();

        return accountRepository
                .findAllByUser_EmailAndStatus(
                        email,
                        AccountStatus.ACTIVE
                )
                .stream()
                .map(this::toAccountResponse)
                .toList();
    }

    // Get one active account belonging to the authenticated user
    public AccountResponse getMyAccountById(Long accountId) {

        String email = currentUserService.getCurrentUserEmail();

        Account account = accountRepository
                .findByIdAndUser_Email(accountId, email)
                .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ACCOUNT_NOT_FOUND
                ));

        return toAccountResponse(account);
    }

    // Deactivate an account without deleting its history
    @Transactional
    public AccountResponse deactivateAccount(Long accountId) {

        String email = currentUserService.getCurrentUserEmail();

        Account account = accountRepository
                .findByIdAndUserEmailForUpdate(accountId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ACCOUNT_NOT_FOUND
                ));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ACCOUNT_NOT_FOUND
            );
        }

        // Only accounts with a zero balance can be deactivated
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Account balance must be zero before deactivation"
            );
        }

        account.setStatus(AccountStatus.INACTIVE);

        Account savedAccount = accountRepository.save(account);

        return toAccountResponse(savedAccount);
    }

    @Transactional
    public AccountResponse updateAccount(
            Long accountId,
            AccountUpdateRequest request
    ) {

        String email = currentUserService.getCurrentUserEmail();

        Account account = accountRepository
                .findByIdAndUserEmailForUpdate(accountId, email)
                .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        ACCOUNT_NOT_FOUND
                ));

        boolean nameProvided = request.getName() != null;
        boolean typeProvided = request.getType() != null;

        if (!nameProvided && !typeProvided) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one field must be provided"
            );
        }

        if (nameProvided) {
            String name = request.getName().trim();

            if (name.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Account name cannot be blank"
                );
            }

            account.setName(name);
        }

        if (typeProvided && request.getType() != account.getType()) {

            if (account.getBalance() == null
                    || account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Account balance must be zero before changing account type"
                );
            }

            boolean hasTransactions =
                    transactionRepository
                            .existsByFromAccount_IdOrToAccount_Id(
                                    accountId,
                                    accountId
                            );

            if (hasTransactions) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Account type cannot be changed because transaction history exists"
                );
            }

            account.setType(request.getType());
        }

        Account updatedAccount = accountRepository.save(account);

        return toAccountResponse(updatedAccount);
    }

    // Convert Account entity into AccountResponse DTO
    private AccountResponse toAccountResponse(Account account) {

        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getType(),
                account.getBalance(),
                account.getStatus()
        );
    }
}