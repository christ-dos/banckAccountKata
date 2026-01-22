package com.bank.adapter.in.rest.dto;

import com.bank.domain.model.AccountType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AccountDto(
        UUID accountId,
        BigDecimal balance,
        String currency,
        AccountType accountType,
        BigDecimal overdraftLimit,
        BigDecimal depositLimit
) {
}
