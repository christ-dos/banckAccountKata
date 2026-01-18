package com.bank.adapter.out.persistence.mapper;

import com.bank.adapter.out.persistence.entity.AccountEntity;
import com.bank.domain.model.Account;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountMapper.
 */
class AccountMapperTest {

    private final AccountMapper mapper = Mappers.getMapper(AccountMapper.class);

    // ========================================
    // DOMAIN TO ENTITY MAPPING TESTS
    // ========================================

    @Test
    void test_toAccountEntity_should_map_all_fields_correctly() {
        // Given
        UUID accountId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        Account account = new Account(
                accountId,
                new BigDecimal("100.50"),
                "EUR",
                createdAt
        );

        // When
        AccountEntity entity = mapper.toAccountEntity(account);

        // Then
        assertNotNull(entity);
        assertEquals(accountId, entity.getAccountId());
        assertEquals(new BigDecimal("100.50"), entity.getBalance());
        assertEquals("EUR", entity.getCurrency());
        assertEquals(createdAt, entity.getCreatedAt());
    }

    @Test
    void test_toAccountEntity_should_handle_zero_balance() {
        // Given
        Account account = Account.create("USD");

        // When
        AccountEntity entity = mapper.toAccountEntity(account);

        // Then
        assertNotNull(entity);
        assertEquals(BigDecimal.ZERO, entity.getBalance());
        assertEquals("USD", entity.getCurrency());
    }

    // ========================================
    // ENTITY TO DOMAIN MAPPING TESTS
    // ========================================

    @Test
    void test_toAccountDomain_should_map_all_fields_correctly() {
        // Given
        UUID accountId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        AccountEntity entity = new AccountEntity(
                accountId,
                new BigDecimal("250.75"),
                "GBP",
                createdAt
        );

        // When
        Account account = mapper.toAccountDomain(entity);

        // Then
        assertNotNull(account);
        assertEquals(accountId, account.getAccountId());
        assertEquals(new BigDecimal("250.75"), account.getBalance());
        assertEquals("GBP", account.getCurrency());
        assertEquals(createdAt, account.getCreatedAt());
    }

    @Test
    void test_toAccountDomain_should_handle_null_entity() {
        // Given
        AccountEntity entity = null;

        // When
        Account account = mapper.toAccountDomain(entity);

        // Then
        assertNull(account);
    }

    // ========================================
    // BIDIRECTIONAL MAPPING TESTS
    // ========================================

    @Test
    void test_bidirectional_mapping_should_preserve_all_data() {
        // Given
        UUID accountId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        Account originalAccount = new Account(
                accountId,
                new BigDecimal("999.99"),
                "JPY",
                createdAt
        );

        // When
        AccountEntity entity = mapper.toAccountEntity(originalAccount);
        Account mappedAccount = mapper.toAccountDomain(entity);

        // Then
        assertNotNull(mappedAccount);
        assertEquals(originalAccount.getAccountId(), mappedAccount.getAccountId());
        assertEquals(originalAccount.getBalance(), mappedAccount.getBalance());
        assertEquals(originalAccount.getCurrency(), mappedAccount.getCurrency());
        assertEquals(originalAccount.getCreatedAt(), mappedAccount.getCreatedAt());
    }
}
