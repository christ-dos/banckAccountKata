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

    public static Account create(String currency) {
        return new Account(
                UUID.randomUUID(),
                BigDecimal.ZERO,
                currency != null ? currency : "EUR",
                OffsetDateTime.now()
        );
    }

    public void deposit (BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive and not null");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw (BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive and not null");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for withdrawal: " + amount);
        }
        this.balance = this.balance.subtract(amount);
    }
}
