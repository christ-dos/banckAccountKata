package com.bank.domain.port.out;

import com.bank.domain.model.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountPort {
    Account save(Account account);
    Optional<Account> findById(UUID accountId);
}
