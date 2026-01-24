package com.bank.adapter.in.rest.mapper;

import com.bank.adapter.in.rest.dto.AccountStatementDto;
import com.bank.adapter.in.rest.dto.OperationDto;
import com.bank.domain.model.AccountStatement;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

/**
 * MapStruct mapper to convert AccountStatement domain objects to DTOs.
 */
@Mapper(componentModel = "spring")
public abstract class AccountStatementDtoMapper {

    @Autowired
    protected OperationDtoMapper operationDtoMapper;

    /**
     * Converts an AccountStatement domain object to an AccountStatementDto.
     *
     * @param accountStatement the domain object
     * @return the DTO for the API response
     */
    public AccountStatementDto toAccountStatementDto(AccountStatement accountStatement) {
        if (accountStatement == null) {
            return null;
        }

        Page<OperationDto> operationDtos = accountStatement.operations()
                .map(operationDtoMapper::toOperationDto);

        return new AccountStatementDto(
                accountStatement.accountId(),
                accountStatement.accountType(),
                accountStatement.balanceAtEndDate(),
                accountStatement.currency(),
                accountStatement.periodStart(),
                accountStatement.periodEnd(),
                operationDtos
        );
    }
}
