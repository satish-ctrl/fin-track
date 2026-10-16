package com.fintrack.controller;

import com.fintrack.dto.AccountCreateRequest;
import com.fintrack.dto.AccountResponse;
import com.fintrack.dto.AccountUpdateRequest;
import com.fintrack.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/create")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountCreateRequest request) {

        AccountResponse response =
                accountService.createAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllMyAccounts() {

        List<AccountResponse> accounts =
                accountService.getAllMyAccounts();

        return ResponseEntity.ok(accounts);
    }


    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getMyAccountById(
            @PathVariable Long accountId) {

        AccountResponse response =
                accountService.getMyAccountById(accountId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{accountId}/deactivate")
    public ResponseEntity<AccountResponse> deactivateAccount(
            @PathVariable Long accountId) {

        AccountResponse response =
                accountService.deactivateAccount(accountId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountUpdateRequest request
    ) {
        return ResponseEntity.ok(accountService
                                .updateAccount(accountId, request)
        );
    }
}