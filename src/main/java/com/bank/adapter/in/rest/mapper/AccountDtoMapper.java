package com.bank.adapter.in.rest.mapper;

import com.bank.adapter.in.rest.dto.AccountDto;
import com.bank.domain.model.CurrentAccount;
import com.bank.domain.model.SavingsAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper to convert Account domain objects to DTOs sent to the frontend.
 */
@Mapper(componentModel = "spring")
public interface AccountDtoMapper {

    /**
     * Converts a CurrentAccount domain object to an AccountDto.
     * SavingsAccount doesn't have overdraftLimit, so depositLimit is ignored.
     *
     * @param account the CurrentAccount domain object
     * @return the DTO for the frontend
     */
    @Mapping(target = "depositLimit", ignore = true)
    AccountDto toCurrentAccountDto(CurrentAccount account);

    /**
     * Converts a SavingsAccount domain object to an AccountDto.
     * CurrentAccount doesn't have depositLimit, so overdraftLimit is ignored.
     *
     * @param account the SavingsAccount domain object
     * @return the DTO for the frontend
     */
    @Mapping(target = "overdraftLimit", ignore = true)
    AccountDto toSavingsAccountDto(SavingsAccount account);

}
