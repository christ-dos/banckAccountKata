package com.bank.domain.port.in;

import com.bank.domain.model.AccountStatement;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Use case: Get account statement with operation history.
 *
 * Provides a detailed view of account operations over a specified period.
 * Default period is 30 days (rolling month) if not specified.
 */
public interface GetAccountStatementUseCase {

    /**
     * Retrieves account statement with paginated operations for a given period.
     * If period is not specified, defaults to the last 30 days.
     *
     * @param accountId the account identifier
     * @param periodStartDate start date of the period (inclusive), null defaults to 30 days ago
     * @param periodEndDate end date of the period (inclusive), null defaults to today
     * @param page the page number (0-indexed)
     * @param size the number of operations per page
     * @return the account statement with paginated operations
     * @throws com.bank.domain.exception.BankAccountNotFoundException if account does not exist
     * @throws IllegalArgumentException if periodStartDate is after periodEndDate
     */
    AccountStatement getAccountStatement(UUID accountId, LocalDate periodStartDate, LocalDate periodEndDate, int page, int size);
}
