package com.bank.adapter.out.persistence;

import com.bank.adapter.out.persistence.entity.OperationEntity;
import com.bank.adapter.out.persistence.mapper.OperationMapper;
import com.bank.adapter.out.persistence.repository.OperationRepository;
import com.bank.domain.model.Operation;
import com.bank.domain.model.OperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JpaOperationAdapter.
 */
@ExtendWith(MockitoExtension.class)
class JpaOperationAdapterTest {

    private static final UUID TEST_ACCOUNT_ID = UUID.randomUUID();

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private OperationMapper operationMapper;

    @InjectMocks
    private JpaOperationAdapter jpaOperationAdapter;

    private Operation testOperation;
    private OperationEntity testOperationEntity;

    @BeforeEach
    void setUp() {
        testOperation = Operation.create(
                TEST_ACCOUNT_ID,
                OperationType.DEPOSIT,
                new BigDecimal("100.00"),
                new BigDecimal("1100.00")
        );

        testOperationEntity = OperationEntity.builder()
                .operationId(testOperation.operationId())
                .accountId(TEST_ACCOUNT_ID)
                .type(OperationType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .balanceAfter(new BigDecimal("1100.00"))
                .operationDate(OffsetDateTime.now())
                .build();
    }

    // ========================================
    // SAVE OPERATION TESTS
    // ========================================

    @Test
    void save_shouldPersistOperation() {
        // Given
        when(operationMapper.toOperationEntity(testOperation)).thenReturn(testOperationEntity);
        when(operationRepository.save(testOperationEntity)).thenReturn(testOperationEntity);
        when(operationMapper.toOperationDomain(testOperationEntity)).thenReturn(testOperation);

        // When
        Operation result = jpaOperationAdapter.save(testOperation);

        // Then
        assertNotNull(result);
        assertEquals(testOperation.operationId(), result.operationId());
        assertEquals(testOperation.amount(), result.amount());

        verify(operationMapper).toOperationEntity(testOperation);
        verify(operationRepository).save(testOperationEntity);
        verify(operationMapper).toOperationDomain(testOperationEntity);
    }

    @Test
    void save_shouldMapAndPersistCorrectly() {
        // Given
        ArgumentCaptor<OperationEntity> entityCaptor = ArgumentCaptor.forClass(OperationEntity.class);
        when(operationMapper.toOperationEntity(testOperation)).thenReturn(testOperationEntity);
        when(operationRepository.save(any(OperationEntity.class))).thenReturn(testOperationEntity);
        when(operationMapper.toOperationDomain(testOperationEntity)).thenReturn(testOperation);

        // When
        jpaOperationAdapter.save(testOperation);

        // Then
        verify(operationRepository).save(entityCaptor.capture());
        OperationEntity savedEntity = entityCaptor.getValue();
        assertEquals(testOperationEntity, savedEntity);
    }

    // ========================================
    // FIND BY ACCOUNT ID AND PERIOD TESTS
    // ========================================

    @Test
    void findByAccountIdAndPeriod_shouldReturnOperations() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);
        int page = 0;
        int size = 20;

        OffsetDateTime startDateTime = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endDateTime = endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        Page<OperationEntity> entityPage = new PageImpl<>(List.of(testOperationEntity));

        when(operationRepository.findByAccountIdAndOperationDateBetween(
                eq(TEST_ACCOUNT_ID),
                eq(startDateTime),
                eq(endDateTime),
                any(Pageable.class)
        )).thenReturn(entityPage);

        when(operationMapper.toOperationDomain(testOperationEntity)).thenReturn(testOperation);

        // When
        Page<Operation> result = jpaOperationAdapter.findByAccountIdAndPeriod(
                TEST_ACCOUNT_ID,
                startDate,
                endDate,
                page,
                size
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testOperation.operationId(), result.getContent().get(0).operationId());

        verify(operationRepository).findByAccountIdAndOperationDateBetween(
                eq(TEST_ACCOUNT_ID),
                eq(startDateTime),
                eq(endDateTime),
                any(Pageable.class)
        );
    }

    @Test
    void findByAccountIdAndPeriod_shouldConvertDatesToOffsetDateTime() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 15);
        LocalDate endDate = LocalDate.of(2026, 1, 20);

        OffsetDateTime expectedStart = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime expectedEnd = endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        when(operationRepository.findByAccountIdAndOperationDateBetween(
                any(UUID.class),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        // When
        jpaOperationAdapter.findByAccountIdAndPeriod(TEST_ACCOUNT_ID, startDate, endDate, 0, 10);

        // Then
        verify(operationRepository).findByAccountIdAndOperationDateBetween(
                eq(TEST_ACCOUNT_ID),
                eq(expectedStart),
                eq(expectedEnd),
                any(Pageable.class)
        );
    }

    @Test
    void findByAccountIdAndPeriod_shouldReturnEmptyPage_whenNoOperations() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        when(operationRepository.findByAccountIdAndOperationDateBetween(
                any(UUID.class),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        // When
        Page<Operation> result = jpaOperationAdapter.findByAccountIdAndPeriod(
                TEST_ACCOUNT_ID,
                startDate,
                endDate,
                0,
                20
        );

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void findByAccountIdAndPeriod_shouldHandlePagination() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);
        int page = 2;
        int size = 10;

        Page<OperationEntity> entityPage = new PageImpl<>(List.of(testOperationEntity));

        when(operationRepository.findByAccountIdAndOperationDateBetween(
                any(UUID.class),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                any(Pageable.class)
        )).thenReturn(entityPage);

        when(operationMapper.toOperationDomain(any(OperationEntity.class))).thenReturn(testOperation);

        // When
        Page<Operation> result = jpaOperationAdapter.findByAccountIdAndPeriod(
                TEST_ACCOUNT_ID,
                startDate,
                endDate,
                page,
                size
        );

        // Then
        assertNotNull(result);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(operationRepository).findByAccountIdAndOperationDateBetween(
                any(UUID.class),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                pageableCaptor.capture()
        );

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(page, capturedPageable.getPageNumber());
        assertEquals(size, capturedPageable.getPageSize());
    }

    @Test
    void findByAccountIdAndPeriod_shouldSortByOperationDateDesc() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        when(operationRepository.findByAccountIdAndOperationDateBetween(
                any(UUID.class),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        // When
        jpaOperationAdapter.findByAccountIdAndPeriod(TEST_ACCOUNT_ID, startDate, endDate, 0, 20);

        // Then
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(operationRepository).findByAccountIdAndOperationDateBetween(
                any(UUID.class),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                pageableCaptor.capture()
        );

        Pageable capturedPageable = pageableCaptor.getValue();
        assertTrue(capturedPageable.getSort().isSorted());
        assertTrue(capturedPageable.getSort().toString().contains("operationDate"));
        assertTrue(capturedPageable.getSort().toString().contains("DESC"));
    }

    @Test
    void findByAccountIdAndPeriod_shouldMapMultipleEntities() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        OperationEntity entity1 = OperationEntity.builder()
                .operationId(UUID.randomUUID())
                .accountId(TEST_ACCOUNT_ID)
                .type(OperationType.DEPOSIT)
                .amount(new BigDecimal("100"))
                .balanceAfter(new BigDecimal("1100"))
                .operationDate(OffsetDateTime.now())
                .build();

        OperationEntity entity2 = OperationEntity.builder()
                .operationId(UUID.randomUUID())
                .accountId(TEST_ACCOUNT_ID)
                .type(OperationType.WITHDRAW)
                .amount(new BigDecimal("50"))
                .balanceAfter(new BigDecimal("1050"))
                .operationDate(OffsetDateTime.now())
                .build();

        Page<OperationEntity> entityPage = new PageImpl<>(List.of(entity1, entity2));

        Operation operation1 = Operation.create(TEST_ACCOUNT_ID, OperationType.DEPOSIT, new BigDecimal("100"), new BigDecimal("1100"));
        Operation operation2 = Operation.create(TEST_ACCOUNT_ID, OperationType.WITHDRAW, new BigDecimal("50"), new BigDecimal("1050"));

        when(operationRepository.findByAccountIdAndOperationDateBetween(
                any(UUID.class),
                any(OffsetDateTime.class),
                any(OffsetDateTime.class),
                any(Pageable.class)
        )).thenReturn(entityPage);

        when(operationMapper.toOperationDomain(entity1)).thenReturn(operation1);
        when(operationMapper.toOperationDomain(entity2)).thenReturn(operation2);

        // When
        Page<Operation> result = jpaOperationAdapter.findByAccountIdAndPeriod(
                TEST_ACCOUNT_ID,
                startDate,
                endDate,
                0,
                20
        );

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(OperationType.DEPOSIT, result.getContent().get(0).type());
        assertEquals(OperationType.WITHDRAW, result.getContent().get(1).type());

        verify(operationMapper, times(2)).toOperationDomain(any(OperationEntity.class));
    }
}
