package com.bank.adapter.out.persistence.mapper;

import com.bank.adapter.out.persistence.entity.AccountEntity;
import com.bank.domain.model.CurrentAccount;
import com.bank.domain.model.SavingsAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "depositLimit", ignore = true)
    AccountEntity toCurrentAccountEntity(CurrentAccount account);

    CurrentAccount toCurrentAccountDomain(AccountEntity accountEntity);

    @Mapping(target = "overdraftLimit", ignore = true)
    AccountEntity toSavingsAccountEntity(SavingsAccount account);

    @Mapping(target = "overdraftLimit", ignore = true)
    SavingsAccount toSavingsAccountDomain(AccountEntity accountEntity);

}
