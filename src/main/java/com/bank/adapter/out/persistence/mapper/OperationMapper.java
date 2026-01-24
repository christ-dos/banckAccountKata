package com.bank.adapter.out.persistence.mapper;

import com.bank.adapter.out.persistence.entity.OperationEntity;
import com.bank.domain.model.Operation;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for Operation domain model and OperationEntity.
 */
@Mapper(componentModel = "spring")
public interface OperationMapper {

    /**
     * Maps Operation domain model to OperationEntity.
     *
     * @param operation the domain operation
     * @return the operation entity
     */
    OperationEntity toOperationEntity(Operation operation);

    /**
     * Maps OperationEntity to Operation domain model.
     *
     * @param entity the operation entity
     * @return the domain operation
     */
    Operation toOperationDomain(OperationEntity entity);
}