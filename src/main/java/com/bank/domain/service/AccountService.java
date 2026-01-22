package com.bank.domain.service;
import com.bank.adapter.config.BankAccountProperties;
import com.bank.adapter.in.rest.dto.AccountDto;
import com.bank.adapter.in.rest.mapper.AccountDtoMapper;
import com.bank.domain.exception.BankAccountNotFoundException;
import com.bank.domain.model.Account;
import com.bank.domain.model.AccountType;
import com.bank.domain.model.CurrentAccount;
import com.bank.domain.model.SavingsAccount;
import com.bank.domain.port.in.CreateAccountUseCase;
import com.bank.domain.port.in.DepositUseCase;
import com.bank.domain.port.in.GetAccountInfoUseCase;
import com.bank.domain.port.in.WithdrawUseCase;
import com.bank.domain.port.in.SetOverdraftLimitUseCase;
import com.bank.domain.port.out.AccountPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AccountService implements CreateAccountUseCase,
        GetAccountInfoUseCase,
        DepositUseCase,
        WithdrawUseCase,
        SetOverdraftLimitUseCase
{
    private final AccountPort accountPort;
    private final BankAccountProperties bankAccountProperties;
    private final AccountDtoMapper mapper;

    @Override
    @Transactional
    public Account createAccount(AccountType accountType, String currency) {
        if (accountType != AccountType.CURRENT && accountType != AccountType.SAVINGS) {
            throw new IllegalArgumentException("Unsupported account type: " + accountType);
        }

        Account account = buildAccount(accountType, currency);
        return accountPort.save(account);
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

    @Override
    @Transactional
    public void setOverdraftLimit(UUID accountId, BigDecimal overdraftLimit) {
        if (overdraftLimit != null && overdraftLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Overdraft limit must be positive or zero");
        }

        Account account = getAccountDetails(accountId);

        if (account instanceof SavingsAccount) {
            throw new UnsupportedOperationException("Savings accounts cannot have overdraft authorization");
        }

        if (account instanceof CurrentAccount currentAccount) {
            currentAccount.setOverdraftLimit(overdraftLimit);
            accountPort.save(currentAccount);
            log.info("Overdraft limit set to {} for account: {}", overdraftLimit, accountId);
        } else {
            throw new IllegalStateException("Unknown account type");
        }
    }

    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================

    /**
     * Builds an Account domain object based on the account type.
     * This is a private helper method that encapsulates account creation logic.
     *
     * @param accountType the type of account (CURRENT or SAVINGS)
     * @param currency the account currency
     * @return the created account (not yet persisted)
     */
    private Account buildAccount(AccountType accountType, String currency) {
        if (accountType == AccountType.CURRENT) {
            log.info("Creating CURRENT account with currency: {}", currency);
            return CurrentAccount.create(currency);
        }

        BigDecimal depositLimit = bankAccountProperties.getSavings().getDefaultDepositLimit();
        log.info("Creating SAVINGS account with currency: {} and deposit limit: {}", currency, depositLimit);
        return SavingsAccount.create(currency, depositLimit);
    }
}
