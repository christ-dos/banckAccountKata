package com.bank.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Current Account - Standard bank account with optional overdraft authorization.
 * Business rules:
 * - Can have overdraft limit (authorized negative balance)
 * - Withdrawals allowed up to (balance + overdraftLimit)
 */
@Getter
@AllArgsConstructor
public class CurrentAccount implements Account {

    private UUID accountId;
    private BigDecimal balance;
    private String currency;
    private OffsetDateTime createdAt;
    private AccountType accountType = AccountType.CURRENT;
    private BigDecimal overdraftLimit;

    @Override
    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = overdraftLimit;
    }

    /**
     * Factory method to create a new current account without overdraft.
     *
     * @param currency the account currency (defaults to EUR if null or blank)
     * @return a new CurrentAccount instance with zero balance
     */
    public static CurrentAccount create(String currency) {
        return create(currency, null);
    }

    /**
     * Factory method to create a new current account with overdraft limit.
     *
     * @param currency the account currency (defaults to EUR if null or blank)
     * @param overdraftLimit the overdraft authorization limit (must be positive or null)
     * @return a new CurrentAccount instance with zero balance
     * @throws IllegalArgumentException if overdraftLimit is negative
     */
    public static CurrentAccount create(String currency, BigDecimal overdraftLimit) {
        if (overdraftLimit != null && overdraftLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Overdraft limit must be positive");
        }

        return new CurrentAccount(
                UUID.randomUUID(),
                BigDecimal.ZERO,
                (currency != null && !currency.isBlank()) ? currency : "EUR",
                OffsetDateTime.now(),
                AccountType.CURRENT,
                overdraftLimit
        );
    }

    @Override
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive and not null");
        }
        this.balance = this.balance.add(amount);
    }

    @Override
    public void withdraw(BigDecimal amount) {
        validateAmount(amount);
        BigDecimal newBalance = this.balance.subtract(amount);
        validateSufficientFunds(newBalance, amount);
        this.balance = newBalance;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive and not null");
        }
    }

    private void validateSufficientFunds(BigDecimal newBalance, BigDecimal amount) {
        BigDecimal minAllowedBalance = overdraftLimit != null
                ? overdraftLimit.negate()
                : BigDecimal.ZERO;

        if (newBalance.compareTo(minAllowedBalance) < 0) {
            throw new IllegalArgumentException(buildInsufficientFundsMessage(amount));
        }
    }

    private String buildInsufficientFundsMessage(BigDecimal amount) {
        if (overdraftLimit != null) {
            return String.format(
                    "Insufficient funds: withdrawal of %s would exceed overdraft limit of %s",
                    amount, overdraftLimit
            );
        }
        return String.format("Insufficient funds for withdrawal: %s", amount);
    }
}
