package com.bank.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountDto(
    UUID accountId,
    BigDecimal balance,
    String currency
) {}