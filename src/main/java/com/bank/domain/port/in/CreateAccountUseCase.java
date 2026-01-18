package com.bank.domain.port.in;

import com.bank.domain.model.Account;

public interface CreateAccountUseCase {
    Account createAccount(String currency);
}
