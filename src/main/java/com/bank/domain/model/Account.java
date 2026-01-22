package com.bank.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Account contract - defines behavior for all account types.
 * Domain interface - Pure business contract, no implementation details.
 */
public interface Account {

    /**
     * Gets the unique account identifier.
     */
    UUID getAccountId();

    /**
     * Gets the current balance.
     */
    BigDecimal getBalance();

    /**
     * Gets the account currency.
     */
    String getCurrency();

    /**
     * Gets the account creation timestamp.
     */
    OffsetDateTime getCreatedAt();

    /**
     * Gets the account type (CURRENT or SAVINGS).
     */
    AccountType getAccountType();

    /**
     * Deposits money into the account.
     *
     * @param amount the amount to deposit (must be positive)
     * @throws IllegalArgumentException if amount is null or not positive
     */
    void deposit(BigDecimal amount);

    /**
     * Withdraws money from the account.
     *
     * @param amount the amount to withdraw (must be positive)
     * @throws IllegalArgumentException if amount is null, not positive, or insufficient funds
     */
    void withdraw(BigDecimal amount);

    /**
     * Sets the overdraft limit for the account.
     * By default, this operation is not supported and will throw an exception.
     * Implementations like CurrentAccount must override this method.
     *
     * @param limit the new overdraft limit
     * @throws UnsupportedOperationException if the account type does not support overdraft
     */
    default void setOverdraftLimit(BigDecimal limit) {
        throw new UnsupportedOperationException("This account type does not support overdraft authorization.");
    }
}
