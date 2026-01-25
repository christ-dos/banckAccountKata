package com.bank.domain.service;

import com.bank.domain.config.BankAccountProperties;
import com.bank.domain.exception.BankAccountNotFoundException;
import com.bank.domain.model.*;
import com.bank.domain.port.in.*;
import com.bank.domain.port.out.AccountPort;
import com.bank.domain.port.out.OperationPort;
import com.bank.domain.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class AccountService implements CreateAccountUseCase,
        GetAccountInfoUseCase,
        DepositUseCase,
        WithdrawUseCase,
        SetOverdraftLimitUseCase,
        GetAccountStatementUseCase {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountPort accountPort;
    private final OperationPort operationPort;
    private final BankAccountProperties bankAccountProperties;

    @Override
    @Transactional
    public Account createAccount(AccountType accountType, String currency) {
        if (accountType != AccountType.CURRENT && accountType != AccountType.SAVINGS) {
            throw new IllegalArgumentException("Unsupported account type: " + accountType);
        }

        Account account = buildAccount(accountType, currency);
        Account savedAccount = accountPort.save(account);
        log.info("Account created successfully: {} (type: {}, currency: {})",
                savedAccount.getAccountId(), accountType, currency);
        return savedAccount;
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
        Account savedAccount = accountPort.save(account);

        operationPort.save(Operation.create(
                accountId,
                OperationType.DEPOSIT,
                amount,
                savedAccount.getBalance())
        );

        log.info("Deposit of {} completed for account: {}", amount, accountId);
    }

    @Override
    @Transactional
    public void withdraw(UUID accountId, BigDecimal amount) {
        Account account = getAccountDetails(accountId);
        account.withdraw(amount);
        Account accountSaved = accountPort.save(account);

        operationPort.save(Operation.create(accountId, OperationType.WITHDRAW, amount, accountSaved.getBalance()));

        log.info("Withdrawal of {} completed for account: {}", amount, accountId);
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

    @Override
    @Transactional(readOnly = true)
    public AccountStatement getAccountStatement(UUID accountId, LocalDate periodStartDate, LocalDate periodEndDate, int page, int size) {
        Account account = getAccountDetails(accountId);

        LocalDate statementEndDate = determineEndDate(periodEndDate);
        LocalDate statementStartDate = determineStartDate(periodStartDate, statementEndDate);

        validatePeriod(statementStartDate, statementEndDate);

        Page<Operation> operations = operationPort.findByAccountIdAndPeriod(accountId, statementStartDate, statementEndDate, page, size);

        AccountStatement statement = buildAccountStatement(account, statementStartDate, statementEndDate, operations);

        log.info("Account statement generated for account: {} with {} operations",
                accountId, operations.getTotalElements());

        return statement;
    }


    private Account buildAccount(AccountType accountType, String currency) {
        if (accountType == AccountType.CURRENT) {
            log.info("Creating CURRENT account with currency: {}", currency);
            return CurrentAccount.create(currency);
        }

        BigDecimal depositLimit = bankAccountProperties.getSavings().getDefaultDepositLimit();
        log.info("Creating SAVINGS account with currency: {} and deposit limit: {}", currency, depositLimit);
        return SavingsAccount.create(currency, depositLimit);
    }

    private LocalDate determineEndDate(LocalDate periodEndDate) {
        return (periodEndDate != null) ? periodEndDate : LocalDate.now();
    }

    private LocalDate determineStartDate(LocalDate periodStartDate, LocalDate endDate) {
        return (periodStartDate != null) ? periodStartDate : endDate.minusDays(30);
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (DateUtils.isStartDateAfterEndDate(startDate, endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }

    private AccountStatement buildAccountStatement(
            Account account,
            LocalDate startDate,
            LocalDate endDate,
            Page<Operation> operations
    ) {
        BigDecimal balanceAtEndDate = calculateBalanceAtDate(account, endDate, operations);

        return new AccountStatement(
                account.getAccountId(),
                account.getAccountType(),
                balanceAtEndDate,
                account.getCurrency(),
                startDate,
                endDate,
                operations
        );
    }

    private BigDecimal calculateBalanceAtDate(Account account, LocalDate endDate, Page<Operation> operations) {
        if (DateUtils.isDateTodayOrFuture(endDate)) {
            return account.getBalance();
        }

        if (operations.hasContent()) {
            List<Operation> operationList = operations.getContent();
            if (!CollectionUtils.isEmpty(operationList)) {
                return operationList.get(0).balanceAfter();
            }
        }

        return account.getBalance();
    }
}
