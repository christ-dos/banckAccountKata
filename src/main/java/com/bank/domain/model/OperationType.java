package com.bank.domain.model;

/**
 * Type of banking operation.
 * Represents the nature of a transaction on an account.
 */
public enum OperationType {
    /**
     * Money deposited into the account.
     */
    DEPOSIT,

    /**
     * Money withdrawn from the account.
     */
    WITHDRAW
}