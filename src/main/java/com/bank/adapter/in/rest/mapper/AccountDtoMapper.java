package com.bank.adapter.in.rest.mapper;

import com.bank.adapter.in.rest.dto.AccountDto;
import com.bank.domain.model.CurrentAccount;
import com.bank.domain.model.SavingsAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountDtoMapper {

    @Mapping(target = "depositLimit", ignore = true)
    AccountDto toCurrentAccountDto(CurrentAccount account);

    @Mapping(target = "overdraftLimit", ignore = true)
    AccountDto toSavingsAccountDto(SavingsAccount account);

}
