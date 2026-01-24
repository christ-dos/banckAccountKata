package com.bank.adapter.out.persistence;

import com.bank.adapter.out.persistence.entity.OperationEntity;
import com.bank.adapter.out.persistence.mapper.OperationMapper;
import com.bank.adapter.out.persistence.repository.OperationRepository;
import com.bank.domain.model.Operation;
import com.bank.domain.port.out.OperationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JpaOperationAdapter implements OperationPort {

    private final OperationRepository operationRepository;
    private final OperationMapper operationMapper;

    @Override
    public Operation save(Operation operation) {
        OperationEntity savedEntity = operationRepository.save(operationMapper.toOperationEntity(operation));
        log.debug("Operation saved successfully with ID: {}", savedEntity.getOperationId());
        return operationMapper.toOperationDomain(savedEntity);
    }


    @Override
    public Page<Operation> findByAccountIdAndPeriod(UUID accountId, LocalDate periodStartDate, LocalDate periodEndDate, int page, int size) {
        log.debug("Finding operations for account: {} from {} to {} (page={}, size={})",
            accountId, periodStartDate, periodEndDate, page, size);

        OffsetDateTime startDateTime = periodStartDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endDateTime = periodEndDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationDate"));

        Page<Operation> operations = operationRepository
                .findByAccountIdAndOperationDateBetween(accountId, startDateTime, endDateTime, pageable)
                .map(operationMapper::toOperationDomain);


        log.info("Found {} operations for account {}", operations.getTotalElements(), accountId);
        return operations;
    }
}
