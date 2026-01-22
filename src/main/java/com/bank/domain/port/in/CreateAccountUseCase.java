package com.bank.domain.port.in;

import com.bank.domain.model.Account;
import com.bank.domain.model.AccountType;

/**
 * Use case for creating bank accounts (current and savings).
 */
public interface CreateAccountUseCase {

    /**
     * Creates a new bank account based on the account type.
     *
     * @param accountType the type of account to create (CURRENT or SAVINGS)
     * @param currency the account currency (defaults to EUR if null or blank)
     * @return the created account with initial balance of zero
     */
    Account createAccount(AccountType accountType, String currency);
}
