package com.bank.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Account {
    private  UUID accountId;
    @Setter
    private BigDecimal balance;
    private String currency;
    private OffsetDateTime createdAt;
    @Setter
    private BigDecimal overdraftLimit;

    public static Account create(String currency) {
        return create(currency, null);
    }

    public static Account create(String currency, BigDecimal overdraftLimit) {
        if (overdraftLimit != null && overdraftLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Overdraft limit must be positive");
        }

        return new Account(
                UUID.randomUUID(),
                BigDecimal.ZERO,
                (currency != null && !currency.isBlank()) ? currency : "EUR",
                OffsetDateTime.now(),
                overdraftLimit
        );
    }

    public void deposit (BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive and not null");
        }
        this.balance = this.balance.add(amount);
    }

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
            throw new IllegalArgumentException(
                    buildInsufficientFundsMessage(amount)
            );
        }
    }

    private String buildInsufficientFundsMessage(BigDecimal amount) {
        if (overdraftLimit != null) {
            BigDecimal available = balance.add(overdraftLimit);
            return String.format(
                    "Insufficient funds: withdrawal would exceed overdraft limit of %s. Available: %s, Requested: %s",
                    overdraftLimit, available, amount
            );
        }
        return String.format("Insufficient funds for withdrawal: %s", amount);
    }
}
