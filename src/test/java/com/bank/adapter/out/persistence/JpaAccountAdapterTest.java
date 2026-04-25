package com.bank.adapter.out.persistence;

import com.bank.adapter.out.persistence.entity.AccountEntity;
import com.bank.adapter.out.persistence.mapper.AccountMapper;
import com.bank.adapter.out.persistence.repository.AccountRepository;
import com.bank.domain.model.Account;
import com.bank.domain.model.AccountType;
import com.bank.domain.model.CurrentAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JpaAccountAdapter.
 */
@ExtendWith(MockitoExtension.class)
class JpaAccountAdapterTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private JpaAccountAdapter jpaAccountAdapter;

    private CurrentAccount testAccount;
    private AccountEntity testEntity;
    private UUID testAccountId;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        testAccount = new CurrentAccount(
                testAccountId,
                new BigDecimal("100.00"),
                "EUR",
                OffsetDateTime.now(),
                AccountType.CURRENT,
                null
        );
        testEntity = AccountEntity.builder()
                .accountId(testAccountId)
                .balance(new BigDecimal("100.00"))
                .currency("EUR")
                .createdAt(OffsetDateTime.now())
                .accountType(AccountType.CURRENT)
                .build();
    }

    // ========================================
    // SAVE TESTS
    // ========================================

    @Test
    void test_save_should_persist_account_successfully() {
        // Given / When
        when(accountMapper.toCurrentAccountEntity(testAccount)).thenReturn(testEntity);
        when(accountRepository.save(testEntity)).thenReturn(testEntity);
        when(accountMapper.toCurrentAccountDomain(testEntity)).thenReturn(testAccount);
        Account savedAccount = jpaAccountAdapter.save(testAccount);

        // Then
        assertNotNull(savedAccount);
        assertEquals(testAccountId, savedAccount.getAccountId());
        verify(accountMapper).toCurrentAccountEntity(testAccount);
        verify(accountRepository).save(testEntity);
        verify(accountMapper).toCurrentAccountDomain(testEntity);
    }

    @Test
    void test_save_should_handle_new_account() {
        // Given
        CurrentAccount newAccount = CurrentAccount.create("USD");
        AccountEntity newEntity = AccountEntity.builder()
                .accountId(newAccount.getAccountId())
                .balance(BigDecimal.ZERO)
                .currency("USD")
                .createdAt(newAccount.getCreatedAt())
                .accountType(AccountType.CURRENT)
                .build();

        // When
        when(accountMapper.toCurrentAccountEntity(newAccount)).thenReturn(newEntity);
        when(accountRepository.save(newEntity)).thenReturn(newEntity);
        when(accountMapper.toCurrentAccountDomain(newEntity)).thenReturn(newAccount);

        Account savedAccount = jpaAccountAdapter.save(newAccount);

        // Then
        assertNotNull(savedAccount);
        assertEquals(BigDecimal.ZERO, savedAccount.getBalance());
        assertEquals("USD", savedAccount.getCurrency());
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void test_save_should_update_existing_account() {
        // Given
        CurrentAccount updatedAccount = new CurrentAccount(
                testAccountId,
                new BigDecimal("250.00"),
                "EUR",
                testAccount.getCreatedAt(),
                AccountType.CURRENT,
                null
        );
        AccountEntity updatedEntity = AccountEntity.builder()
                .accountId(testAccountId)
                .balance(new BigDecimal("250.00"))
                .currency("EUR")
                .createdAt(testAccount.getCreatedAt())
                .accountType(AccountType.CURRENT)
                .build();

        // When
        when(accountMapper.toCurrentAccountEntity(updatedAccount)).thenReturn(updatedEntity);
        when(accountRepository.save(updatedEntity)).thenReturn(updatedEntity);
        when(accountMapper.toCurrentAccountDomain(updatedEntity)).thenReturn(updatedAccount);
        Account savedAccount = jpaAccountAdapter.save(updatedAccount);

        // Then
        assertNotNull(savedAccount);
        assertEquals(new BigDecimal("250.00"), savedAccount.getBalance());
        verify(accountRepository).save(updatedEntity);
    }

    // ========================================
    // FIND BY ID TESTS
    // ========================================

    @Test
    void test_findById_should_return_account_when_exists() {
        // Given / When
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testEntity));
        when(accountMapper.toCurrentAccountDomain(testEntity)).thenReturn(testAccount);
        Optional<Account> foundAccount = jpaAccountAdapter.findById(testAccountId);

        // Then
        assertTrue(foundAccount.isPresent());
        assertEquals(testAccountId, foundAccount.get().getAccountId());
        assertEquals(new BigDecimal("100.00"), foundAccount.get().getBalance());
        verify(accountRepository).findById(testAccountId);
        verify(accountMapper).toCurrentAccountDomain(testEntity);
    }

    @Test
    void test_findById_should_return_empty_when_not_exists() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        Optional<Account> foundAccount = jpaAccountAdapter.findById(nonExistentId);

        // Then
        assertFalse(foundAccount.isPresent());
        verify(accountRepository).findById(nonExistentId);
        verifyNoInteractions(accountMapper);
    }

    // ========================================
    // INTEGRATION SCENARIO TESTS
    // ========================================

    @Test
    void test_save_and_find_integration_scenario() {
        // Given / When - Save
        testEntity.setAccountType(AccountType.CURRENT);
        when(accountMapper.toCurrentAccountEntity(testAccount)).thenReturn(testEntity);
        when(accountRepository.save(testEntity)).thenReturn(testEntity);
        when(accountMapper.toCurrentAccountDomain(testEntity)).thenReturn(testAccount);
        Account savedAccount = jpaAccountAdapter.save(testAccount);

        // Then - Save
        assertNotNull(savedAccount);
        verify(accountRepository).save(testEntity);

        // When - Find
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testEntity));
        Optional<Account> foundAccount = jpaAccountAdapter.findById(testAccountId);

        // Then - Find
        assertTrue(foundAccount.isPresent());
        assertEquals(savedAccount.getAccountId(), foundAccount.get().getAccountId());
    }
}
