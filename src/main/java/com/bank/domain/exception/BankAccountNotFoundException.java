package com.bank.domain.exception;

/**
 * Exception thrown when a bank account is not found.
 */
public class BankAccountNotFoundException extends RuntimeException {

    /**
     * Constructs a new BankAccountNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public BankAccountNotFoundException(String message) {
        super(message);
    }
}
