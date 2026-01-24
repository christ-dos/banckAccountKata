package com.bank.domain.model;

import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Account Statement - Immutable value object representing an account statement.
 * Provides a snapshot of account status and operation history over a period.
 *
 * Business rules:
 * - Statement is a read-only document
 * - Operations are sorted by date in descending order (most recent first)
 * - Default period is 30 days (rolling month)
 * - Statement date is the end date of the period
 */
public record AccountStatement(
    UUID accountId,
    AccountType accountType,
    BigDecimal balanceAtEndDate,
    String currency,
    LocalDate periodStart,
    LocalDate periodEnd,
    Page<Operation> operations
) {
    /**
     * Compact constructor with validation.
     */
    public AccountStatement {
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        Objects.requireNonNull(accountType, "Account type cannot be null");
        Objects.requireNonNull(balanceAtEndDate, "Balance at end date cannot be null");
        Objects.requireNonNull(periodStart, "Period start cannot be null");
        Objects.requireNonNull(periodEnd, "Period end cannot be null");
        Objects.requireNonNull(operations, "Operations cannot be null");


        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("Period start must be before or equal to period end");
        }
    }
}