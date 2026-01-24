package com.bank.adapter.in.rest.dto;

import com.bank.domain.model.AccountType;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO representing an account statement for API responses.
 */
public record AccountStatementDto(
        UUID accountId,
        AccountType accountType,
        BigDecimal balanceAtEndDate,
        String currency,
        LocalDate periodStart,
        LocalDate periodEnd,
        Page<OperationDto> operations
) {
}
