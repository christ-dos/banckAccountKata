package com.bank.domain.port.in;

import java.util.UUID;

public interface CreateAccountUseCase {
    UUID createAccount(String currency);
}
