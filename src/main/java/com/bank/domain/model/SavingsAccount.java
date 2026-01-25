package com.bank.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Savings Account - Bank account with deposit limit and no overdraft allowed.
 * Business rules:
 * - Has a maximum deposit limit (e.g., 22,950€ for Livret A)
 * - CANNOT have overdraft authorization
 * - Balance must always be >= 0
 */
@Getter
@AllArgsConstructor
public class SavingsAccount implements Account {

    private UUID accountId;
    private BigDecimal balance;
    private String currency;
    private OffsetDateTime createdAt;
    private AccountType accountType = AccountType.SAVINGS;
    private BigDecimal depositLimit;

    /**
     * Factory method to create a new savings account with custom deposit limit.
     *
     * @param currency the account currency (defaults to EUR if null or blank)
     * @param depositLimit the maximum deposit limit (must be positive)
     * @return a new SavingsAccount instance with zero balance and initial balance of zero
     * @throws IllegalArgumentException if depositLimit is null or not positive
     */
    public static SavingsAccount create(String currency, BigDecimal depositLimit) {
        if (depositLimit == null || depositLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit limit must be positive");
        }

        return new SavingsAccount(
                UUID.randomUUID(),
                BigDecimal.ZERO,
                (currency != null && !currency.isBlank()) ? currency : "EUR",
                OffsetDateTime.now(),
                AccountType.SAVINGS,
                depositLimit
        );
    }

    @Override
    public void deposit(BigDecimal amount) {
        validateDepositAmount(amount);
        BigDecimal newBalance = this.balance.add(amount);
        validateDepositLimit(newBalance, amount);
        this.balance = newBalance;
    }

    private void validateDepositAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive and not null");
        }
    }

    private void validateDepositLimit(BigDecimal newBalance, BigDecimal amount) {
        if (newBalance.compareTo(depositLimit) > 0) {
            BigDecimal remainingCapacity = calculateRemainingDepositCapacity();
            throw new IllegalArgumentException(
                    buildDepositLimitExceededMessage(amount, remainingCapacity)
            );
        }
    }

    private BigDecimal calculateRemainingDepositCapacity() {
        return depositLimit.subtract(balance);
    }

    private String buildDepositLimitExceededMessage(BigDecimal requestedAmount, BigDecimal remainingCapacity) {
        if (remainingCapacity.compareTo(BigDecimal.ZERO) <= 0) {
            return String.format(
                    "Cannot deposit %s: deposit limit of %s already reached.",
                    requestedAmount, depositLimit
            );
        }
        return String.format(
                "Cannot deposit %s: would exceed deposit limit of %s. Maximum allowed deposit: %s",
                requestedAmount, depositLimit, remainingCapacity
        );
    }

    @Override
    public void withdraw(BigDecimal amount) {
        validateWithdrawalAmount(amount);
        validateSufficientBalance(amount);
        this.balance = this.balance.subtract(amount);
    }

    private void validateWithdrawalAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive and not null");
        }
    }

    private void validateSufficientBalance(BigDecimal amount) {
        // Savings accounts CANNOT have overdraft - strict balance check
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException(String.format(
                    "Insufficient funds for withdrawal: %s",
                    amount
            ));
        }
    }

    /**
     * Savings accounts cannot have overdraft authorization.
     *
     * @param limit ignored
     * @throws UnsupportedOperationException always, as savings accounts don't support overdraft
     */
    public void setOverdraftLimit(BigDecimal limit) {
        throw new UnsupportedOperationException("Savings accounts cannot have overdraft authorization");
    }
}
