package com.bank.adapter.out.persistence.repository;

import com.bank.adapter.out.persistence.entity.OperationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public interface OperationRepository extends JpaRepository<OperationEntity, UUID> {

    /**
     * Finds operations by account ID and date range (between).
     * Spring Data generates the query automatically based on method name.
     *
     * @param accountId the account identifier
     * @param startDate start date (inclusive)
     * @param endDate end date (exclusive)
     * @param pageable pagination parameters
     * @return a page of operations
     */
    Page<OperationEntity> findByAccountIdAndOperationDateBetween(
        UUID accountId,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        Pageable pageable
    );
}