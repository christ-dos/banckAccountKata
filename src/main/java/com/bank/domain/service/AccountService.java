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

    // ========================================
    // PRIVATE HELPER METHODS
    // ========================================

    /**
     * Builds an Account domain object based on the account type.
     * This is a private helper method that encapsulates account creation logic.
     *
     * @param accountType the type of account (CURRENT or SAVINGS)
     * @param currency    the account currency
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

    /**
     * Determines the end date for the statement period.
     * Defaults to current date if not specified.
     *
     * @param periodEndDate the requested end date (can be null)
     * @return the end date (today if null)
     */
    private LocalDate determineEndDate(LocalDate periodEndDate) {
        return (periodEndDate != null) ? periodEndDate : LocalDate.now();
    }

    /**
     * Determines the start date for the statement period.
     * Defaults to 30 days before the end date if not specified.
     *
     * @param periodStartDate the requested start date (can be null)
     * @param endDate the end date of the period
     * @return the start date (endDate - 30 days if null)
     */
    private LocalDate determineStartDate(LocalDate periodStartDate, LocalDate endDate) {
        return (periodStartDate != null) ? periodStartDate : endDate.minusDays(30);
    }

    /**
     * Validates that the start date is not after the end date.
     *
     * @param startDate the start date of the period
     * @param endDate the end date of the period
     * @throws IllegalArgumentException if start date is after end date
     */
    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (DateUtils.isStartDateAfterEndDate(startDate, endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }
    }

    /**
     * Builds an AccountStatement from account details and operations.
     *
     * @param account the account
     * @param startDate the start date of the period
     * @param endDate the end date of the period
     * @param operations the paginated operations (already loaded from DB)
     * @return the account statement
     */
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

    /**
     * Calculates the account balance at the end date using already loaded operations.
     * This avoids an additional database query.
     *
     * If the date is today or in the future, returns the current balance.
     * Otherwise, uses the balanceAfter from the most recent operation in the loaded list.
     *
     * @param account the account
     * @param endDate the date for which to calculate the balance
     * @param operations the already loaded operations for this period (sorted DESC)
     * @return the balance at the specified date
     */
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

        // No operations in this period, return current account balance
        return account.getBalance();
    }
}
