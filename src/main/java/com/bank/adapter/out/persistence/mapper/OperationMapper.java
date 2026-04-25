package com.bank.adapter.out.persistence.mapper;

import com.bank.adapter.out.persistence.entity.OperationEntity;
import com.bank.domain.model.Operation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OperationMapper {

    OperationEntity toOperationEntity(Operation operation);

    Operation toOperationDomain(OperationEntity entity);
}