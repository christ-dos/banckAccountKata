package com.bank.domain.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Use case for configuring the overdraft limit on a bank account.
 */
public interface SetOverdraftLimitUseCase {

    /**
     * Sets or updates the authorized overdraft limit for an account.
     *
     * @param accountId the unique identifier of the account
     * @param overdraftLimit the overdraft limit to set (must be >= 0, null to remove)
     * @throws com.bank.domain.exception.BankAccountNotFoundException if account does not exist
     * @throws IllegalArgumentException if overdraftLimit is negative
     */
    void setOverdraftLimit(UUID accountId, BigDecimal overdraftLimit);
}
