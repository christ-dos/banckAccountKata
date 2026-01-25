package com.bank.domain.model;

/**
 * Enumeration of account types.
 * Defines the different types of bank accounts available.
 */
public enum AccountType {
    /**
     * Current account - supports overdraft authorization.
     */
    CURRENT,

    /**
     * Savings account - has deposit limits, cannot have overdraft.
     */
    SAVINGS
}
