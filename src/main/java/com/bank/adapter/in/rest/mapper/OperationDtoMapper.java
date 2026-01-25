package com.bank.adapter.in.rest.mapper;

import com.bank.adapter.in.rest.dto.OperationDto;
import com.bank.domain.model.Operation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OperationDtoMapper {

    OperationDto toOperationDto(Operation operation);
}
