package com.bank.adapter.in.rest.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new bank account.
 * If currency is null or not provided, EUR will be used as default.
 */
public record CreateAccountRequest(
        @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters (e.g., USD, EUR)")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters (e.g., USD, EUR)")
        String currency
) {}
