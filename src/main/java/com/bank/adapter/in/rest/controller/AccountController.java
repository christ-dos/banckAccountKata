package com.bank.adapter.in.rest.controller;

import com.bank.adapter.in.rest.api.ApiDocAccountController;
import com.bank.adapter.in.rest.dto.AccountDto;
import com.bank.adapter.in.rest.request.CreateAccountRequest;
import com.bank.adapter.in.rest.request.DepositRequest;
import com.bank.adapter.in.rest.request.WithdrawRequest;
import com.bank.adapter.in.rest.request.SetOverdraftLimitRequest;
import com.bank.adapter.in.rest.mapper.AccountDtoMapper;
import com.bank.domain.model.Account;
import com.bank.domain.model.CurrentAccount;
import com.bank.domain.model.SavingsAccount;
import com.bank.domain.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for Bank Account operations.
 * OpenAPI documentation is defined in {@link ApiDocAccountController} interface.
 */
@Slf4j
@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
public class AccountController implements ApiDocAccountController {

    private final AccountService accountService;
    private final AccountDtoMapper mapper;

    @PostMapping
    @Override
    public ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request.accountType(), request.currency());

        log.info("{} account created successfully with ID: {}", request.accountType(), account.getAccountId());

        return ResponseEntity
                .status(201)
                .header("Location", "/v1/accounts/" + account.getAccountId())
                .body(toDtoAccount(account));
    }

    @GetMapping("/{accountId}")
    @Override
    public ResponseEntity<AccountDto> getAccountDetails(@PathVariable UUID accountId) {
        Account account = accountService.getAccountDetails(accountId);

        log.info("Account details retrieved for accountId: {}", accountId);

        return ResponseEntity.ok(toDtoAccount(account));
    }

    @PostMapping("/{accountId}/deposit")
    @Override
    public ResponseEntity<AccountDto> deposit(@PathVariable UUID accountId, @Valid @RequestBody DepositRequest request) {
        accountService.deposit(accountId, request.amount());
        Account updatedAccount = accountService.getAccountDetails(accountId);


        log.info("Deposit successful for account: {}", accountId);

        return ResponseEntity.ok(toDtoAccount(updatedAccount));
    }

    @PostMapping("/{accountId}/withdraw")
    @Override
    public ResponseEntity<AccountDto> withdraw(@PathVariable UUID accountId, @Valid @RequestBody WithdrawRequest request) {
        accountService.withdraw(accountId, request.amount());
        Account updatedAccount = accountService.getAccountDetails(accountId);

        log.info("Withdrawal successful for account: {}", accountId);

        return ResponseEntity.ok(toDtoAccount(updatedAccount));
    }

    @PutMapping("/{accountId}/overdraft")
    @Override
    public ResponseEntity<AccountDto> setOverdraftLimit(@PathVariable UUID accountId, @Valid @RequestBody SetOverdraftLimitRequest request) {
        accountService.setOverdraftLimit(accountId, request.overdraftLimit());
        Account updatedAccount = accountService.getAccountDetails(accountId);

        log.info("Overdraft limit set to {} for account: {}", request.overdraftLimit(), accountId);

        return ResponseEntity.ok(toDtoAccount(updatedAccount));
    }

    private AccountDto toDtoAccount(Account account) {
        return account instanceof CurrentAccount ?
                mapper.toCurrentAccountDto((CurrentAccount) account) :
                mapper.toSavingsAccountDto((SavingsAccount) account);
    }
}
