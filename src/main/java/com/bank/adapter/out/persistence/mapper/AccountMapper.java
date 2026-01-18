package com.bank.adapter.out.persistence.mapper;

import com.bank.adapter.out.persistence.entity.AccountEntity;
import com.bank.domain.model.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountEntity toAccountEntity(Account account);
    Account toAccountDomain(AccountEntity accountEntity);
}
