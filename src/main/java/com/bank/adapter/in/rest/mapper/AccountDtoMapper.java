package com.bank.adapter.in.rest.mapper;

import com.bank.adapter.in.rest.dto.AccountDto;
import com.bank.domain.model.Account;
import com.bank.domain.model.CurrentAccount;
import com.bank.domain.model.SavingsAccount;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper to convert Account domain objects to DTOs sent to the frontend.
 */
@Mapper(componentModel = "spring")
public interface AccountDtoMapper {

    /**
     * Converts an Account domain object to an AccountDto.
     *
     * @param account the domain object
     * @return the DTO for the frontend
     */
    AccountDto toCurrentAccountDto(CurrentAccount account);
    AccountDto toSavingsAccountDto(SavingsAccount account);

}
