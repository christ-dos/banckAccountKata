package com.bank.adapter.out.persistence.mapper;

import com.bank.adapter.out.persistence.entity.AccountEntity;
import com.bank.domain.model.AccountType;
import com.bank.domain.model.CurrentAccount;
import com.bank.domain.model.SavingsAccount;
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

    private static final String EUR = "EUR";
    private final AccountMapper mapper = Mappers.getMapper(AccountMapper.class);

    // ========================================
    // CURRENT ACCOUNT TESTS
    // ========================================

    @Test
    void test_toAccountEntity_should_map_current_account_correctly() {
        // Given
        UUID accountId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        CurrentAccount currentAccount = new CurrentAccount(
                accountId,
                new BigDecimal("100.50"),
                EUR,
                createdAt,
                AccountType.CURRENT,
                new BigDecimal("500.00")
        );

        // When
        AccountEntity entity = mapper.toCurrentAccountEntity(currentAccount);

        // Then
        assertNotNull(entity);
        assertEquals(accountId, entity.getAccountId());
        assertEquals(new BigDecimal("100.50"), entity.getBalance());
        assertEquals(EUR, entity.getCurrency());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(AccountType.CURRENT, entity.getAccountType());
        assertEquals(new BigDecimal("500.00"), entity.getOverdraftLimit());
        assertNull(entity.getDepositLimit());
    }

    @Test
    void test_toAccountDomain_should_map_current_account_correctly() {
        // Given
        UUID accountId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        AccountEntity entity = AccountEntity.builder()
                .accountId(accountId)
                .balance(new BigDecimal("250.75"))
                .currency(EUR)
                .createdAt(createdAt)
                .accountType(AccountType.CURRENT)
                .overdraftLimit(new BigDecimal("300.00"))
                .build();

        // When
        CurrentAccount account = mapper.toCurrentAccountDomain(entity);

        // Then
        assertNotNull(account);
        assertEquals(accountId, account.getAccountId());
        assertEquals(new BigDecimal("250.75"), account.getBalance());
        assertEquals(EUR, account.getCurrency());
        assertEquals(createdAt, account.getCreatedAt());
        assertEquals(new BigDecimal("300.00"), account.getOverdraftLimit());
    }

    // ========================================
    // SAVINGS ACCOUNT TESTS
    // ========================================

    @Test
    void test_toAccountEntity_should_map_savings_account_correctly() {
        // Given
        UUID accountId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        SavingsAccount savingsAccount = new SavingsAccount(
                accountId,
                new BigDecimal("5000.00"),
                EUR,
                createdAt,
                AccountType.SAVINGS,
                new BigDecimal("22950.00")
        );

        // When
        AccountEntity entity = mapper.toSavingsAccountEntity(savingsAccount);

        // Then
        assertNotNull(entity);
        assertEquals(accountId, entity.getAccountId());
        assertEquals(new BigDecimal("5000.00"), entity.getBalance());
        assertEquals(EUR, entity.getCurrency());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(AccountType.SAVINGS, entity.getAccountType());
        assertEquals(new BigDecimal("22950.00"), entity.getDepositLimit());
        assertNull(entity.getOverdraftLimit());
    }

    @Test
    void test_toAccountDomain_should_map_savings_account_correctly() {
        // Given
        UUID accountId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        AccountEntity entity = AccountEntity.builder()
                .accountId(accountId)
                .balance(new BigDecimal("10000.00"))
                .currency(EUR)
                .createdAt(createdAt)
                .accountType(AccountType.SAVINGS)
                .depositLimit(new BigDecimal("22950.00"))
                .build();

        // When
        SavingsAccount savingsAccount = mapper.toSavingsAccountDomain(entity);

        // Then
        assertNotNull(savingsAccount);
        assertEquals(accountId, savingsAccount.getAccountId());
        assertEquals(new BigDecimal("10000.00"), savingsAccount.getBalance());
        assertEquals(EUR, savingsAccount.getCurrency());
        assertEquals(createdAt, savingsAccount.getCreatedAt());
        assertEquals(new BigDecimal("22950.00"), savingsAccount.getDepositLimit());
    }

    // ========================================
    // NULL HANDLING TESTS
    // ========================================

    @Test
    void test_toCurrentAccountEntity_should_handle_null_account() {
        // Given
        CurrentAccount account = null;

        // When
        AccountEntity entity = mapper.toCurrentAccountEntity(account);

        // Then
        assertNull(entity);
    }

    @Test
    void test_toCurrentAccountDomain_should_handle_null_entity() {
        // Given
        AccountEntity entity = null;

        // When
        CurrentAccount account = mapper.toCurrentAccountDomain(entity);

        // Then
        assertNull(account);
    }

    // ========================================
    // BIDIRECTIONAL MAPPING TESTS
    // ========================================

    @Test
    void test_bidirectional_mapping_should_preserve_current_account_data() {
        // Given
        UUID accountId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        CurrentAccount originalAccount = new CurrentAccount(
                accountId,
                new BigDecimal("999.99"),
                EUR,
                createdAt,
                AccountType.CURRENT,
                new BigDecimal("200.00")
        );

        // When
        AccountEntity entity = mapper.toCurrentAccountEntity(originalAccount);
        CurrentAccount mappedAccount = mapper.toCurrentAccountDomain(entity);

        // Then
        assertNotNull(mappedAccount);
        assertEquals(originalAccount.getAccountId(), mappedAccount.getAccountId());
        assertEquals(originalAccount.getBalance(), mappedAccount.getBalance());
        assertEquals(originalAccount.getCurrency(), mappedAccount.getCurrency());
        assertEquals(originalAccount.getCreatedAt(), mappedAccount.getCreatedAt());
        assertEquals(originalAccount.getOverdraftLimit(), mappedAccount.getOverdraftLimit());
    }

    @Test
    void test_bidirectional_mapping_should_preserve_savings_account_data() {
        // Given
        UUID accountId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now();
        SavingsAccount originalAccount = new SavingsAccount(
                accountId,
                new BigDecimal("15000.00"),
                EUR,
                createdAt,
                AccountType.SAVINGS,
                new BigDecimal("22950.00")
        );

        // When
        AccountEntity entity = mapper.toSavingsAccountEntity(originalAccount);
        SavingsAccount mappedAccount = mapper.toSavingsAccountDomain(entity);

        // Then
        assertNotNull(mappedAccount);
        assertEquals(originalAccount.getAccountId(), mappedAccount.getAccountId());
        assertEquals(originalAccount.getBalance(), mappedAccount.getBalance());
        assertEquals(originalAccount.getCurrency(), mappedAccount.getCurrency());
        assertEquals(originalAccount.getCreatedAt(), mappedAccount.getCreatedAt());
        assertEquals(originalAccount.getDepositLimit(), mappedAccount.getDepositLimit());
    }
}
