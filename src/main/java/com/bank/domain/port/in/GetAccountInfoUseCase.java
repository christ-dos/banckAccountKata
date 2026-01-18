package com.bank.domain.port.in;

import com.bank.domain.model.Account;

import java.math.BigDecimal;
import java.util.UUID;

public interface GetAccountInfoUseCase {
    Account getAccountDetails(UUID accountId);
    BigDecimal getBalance(UUID accountId);
}
