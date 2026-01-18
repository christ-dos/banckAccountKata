package com.bank.adapter.out.persistence;

import com.bank.adapter.out.persistence.entity.AccountEntity;
import com.bank.adapter.out.persistence.mapper.AccountMapper;
import com.bank.adapter.out.persistence.repository.AccountRepository;
import com.bank.domain.model.Account;
import com.bank.domain.port.out.AccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaAccountAdapter implements AccountPort {

    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;

    @Override
    public Account save(Account account) {
        AccountEntity savedEntity = accountRepository.save(
                accountMapper.toAccountEntity(account)
        );
        return accountMapper.toAccountDomain(savedEntity);
    }

    @Override
    public Optional<Account> findById(UUID accountId) {
        return accountRepository.findById(accountId)
                .map(accountMapper::toAccountDomain);
    }
}
