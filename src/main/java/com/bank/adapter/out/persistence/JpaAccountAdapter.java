package com.bank.adapter.out.persistence;

import com.bank.adapter.out.persistence.entity.AccountEntity;
import com.bank.adapter.out.persistence.mapper.AccountMapper;
import com.bank.adapter.out.persistence.repository.AccountRepository;
import com.bank.domain.model.Account;
import com.bank.domain.model.AccountType;
import com.bank.domain.model.CurrentAccount;
import com.bank.domain.model.SavingsAccount;
import com.bank.domain.port.out.AccountPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter for Account persistence operations.
 */
@Component
@RequiredArgsConstructor
public class JpaAccountAdapter implements AccountPort {

    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;

    @Override
    public Account save(Account account) {
        AccountEntity entityToSave = mapDomainToEntity(account);
        AccountEntity savedEntity = accountRepository.save(entityToSave);
        return mapEntityToDomain(savedEntity);
    }

    @Override
    public Optional<Account> findById(UUID accountId) {
        return accountRepository.findById(accountId)
                .map(this::mapEntityToDomain);
    }

    private AccountEntity mapDomainToEntity(Account account) {
        if (account instanceof CurrentAccount) {
            return accountMapper.toCurrentAccountEntity((CurrentAccount) account);
        } else if (account instanceof SavingsAccount) {
            return accountMapper.toSavingsAccountEntity((SavingsAccount) account);
        }
        throw new IllegalArgumentException("Unsupported account type: " + account.getClass().getName());
    }

    private Account mapEntityToDomain(AccountEntity entity) {
        if (AccountType.CURRENT.equals(entity.getAccountType())) {
            return accountMapper.toCurrentAccountDomain(entity);
        } else if (AccountType.SAVINGS.equals(entity.getAccountType())) {
            return accountMapper.toSavingsAccountDomain(entity);
        }
        throw new IllegalArgumentException("Unknown account type: " + entity.getAccountType());
    }
}
