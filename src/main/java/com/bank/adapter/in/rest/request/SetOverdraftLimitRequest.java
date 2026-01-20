package com.bank.adapter.in.rest.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO for setting overdraft limit on an account.
 */
public record SetOverdraftLimitRequest(
        @NotNull(message = "Overdraft limit is required")
        @DecimalMin(value = "0.0", message = "Overdraft limit must be positive or zero")
        BigDecimal overdraftLimit
) {
}
