package com.bank.adapter.in.rest.mapper;

import com.bank.adapter.in.rest.dto.OperationDto;
import com.bank.domain.model.Operation;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper to convert Operation domain objects to DTOs.
 */
@Mapper(componentModel = "spring")
public interface OperationDtoMapper {

    /**
     * Converts an Operation domain object to an OperationDto.
     *
     * @param operation the domain object
     * @return the DTO for the API response
     */
    OperationDto toOperationDto(Operation operation);
}
