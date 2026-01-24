package com.bank.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Operation - Immutable value object representing a banking operation.
 * Records the history of deposits and withdrawals on an account.
 *
 * Business rules:
 * - An operation cannot be modified once created (immutable)
 * - Amount must be positive
 * - Operation represents a historical fact
 */
public record Operation(
    UUID operationId,
    UUID accountId,
    OperationType type,
    BigDecimal amount,
    BigDecimal balanceAfter,
    OffsetDateTime operationDate
) {

    /**
     * Compact constructor with validation.
     * Ensures business rules are enforced.
     */
    public Operation {
        Objects.requireNonNull(operationId, "Operation ID cannot be null");
        Objects.requireNonNull(accountId, "Account ID cannot be null");
        Objects.requireNonNull(type, "Operation type cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(balanceAfter, "Balance after operation cannot be null");
        Objects.requireNonNull(operationDate, "Operation date cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    /**
     * Factory method to create a new operation.
     *
     * @param accountId the account identifier
     * @param type the operation type (DEPOSIT or WITHDRAW)
     * @param amount the operation amount (must be positive)
     * @param balanceAfter the account balance after the operation
     * @return a new Operation instance
     */
    public static Operation create(UUID accountId, OperationType type, BigDecimal amount, BigDecimal balanceAfter) {
        return new Operation(
            UUID.randomUUID(),
            accountId,
            type,
            amount,
            balanceAfter,
            OffsetDateTime.now()
        );
    }
}