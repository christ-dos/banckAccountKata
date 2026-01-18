package com.bank.domain.service;

import com.bank.domain.exception.BankAccountNotFoundException;
import com.bank.domain.model.Account;
import com.bank.domain.port.in.CreateAccountUseCase;
import com.bank.domain.port.in.DepositUseCase;
import com.bank.domain.port.in.GetAccountInfoUseCase;
import com.bank.domain.port.in.WithdrawUseCase;
import com.bank.domain.port.out.AccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
public class AccountService implements CreateAccountUseCase,
        GetAccountInfoUseCase,
        DepositUseCase,
        WithdrawUseCase
{
    private final AccountPort accountPort;

    @Override
    @Transactional
    public UUID createAccount(String currency) {
        return accountPort.save(Account.create(currency)).getAccountId();
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountDetails(UUID accountId) {
        return accountPort.findById(accountId)
                .orElseThrow(() ->
                        new BankAccountNotFoundException("Bank account not found: " + accountId));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID accountId) {
        return getAccountDetails(accountId).getBalance();
    }

    @Override
    @Transactional
    public void deposit(UUID accountId, BigDecimal amount) {
        Account account = getAccountDetails(accountId);
        account.deposit(amount);
        accountPort.save(account);
    }

    @Override
    @Transactional
    public void withdraw(UUID accountId, BigDecimal amount) {
        Account account = getAccountDetails(accountId);
        account.withdraw(amount);
        accountPort.save(account);
    }
}
