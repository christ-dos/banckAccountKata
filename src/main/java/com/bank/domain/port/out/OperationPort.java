package com.bank.domain.port.out;

import com.bank.domain.model.Operation;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Port for operation persistence operations.
 * Secondary port (driven) - implemented by the persistence adapter.
 */
public interface OperationPort {

    /**
     * Saves an operation to the repository.
     *
     * @param operation the operation to save
     * @return the saved operation
     */
    Operation save(Operation operation);

    /**
     * Retrieves paginated operations for an account within a period.
     * Operations are sorted by date in descending order (most recent first).
     *
     * @param accountId the account identifier
     * @param periodStartDate start date (inclusive)
     * @param periodEndDate end date (inclusive)
     * @param page the page number (0-indexed)
     * @param size the number of operations per page
     * @return a page of operations
     */
    Page<Operation> findByAccountIdAndPeriod(UUID accountId, LocalDate periodStartDate, LocalDate periodEndDate, int page, int size);
}