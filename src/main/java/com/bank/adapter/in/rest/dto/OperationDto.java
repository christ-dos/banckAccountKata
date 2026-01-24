package com.bank.adapter.in.rest.dto;

import com.bank.domain.model.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO representing an operation for API responses.
 */
public record OperationDto(
        UUID operationId,
        UUID accountId,
        OperationType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        OffsetDateTime operationDate
) {
}
