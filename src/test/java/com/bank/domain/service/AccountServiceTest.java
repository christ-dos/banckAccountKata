package com.bank.domain.service;

import com.bank.domain.exception.BankAccountNotFoundException;
import com.bank.domain.model.Account;
import com.bank.domain.port.out.AccountPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.util.stream.Stream;
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
 * Unit tests for AccountService.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountPort accountPort;

    @InjectMocks
    private AccountService accountService;

    private UUID testAccountId;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccountId = UUID.randomUUID();
        testAccount = new Account(
                testAccountId,
                new BigDecimal("100.00"),
                "EUR",
                OffsetDateTime.now()
        );
    }

    // ========================================
    // METHOD SOURCES FOR PARAMETERIZED TESTS
    // ========================================

    private static Stream<Arguments> depositInvalidScenariosProvider() {
        Account testAccount = new Account(UUID.randomUUID(), new BigDecimal("100.00"), "EUR", OffsetDateTime.now());
        return Stream.of(
                // Account not found - should throw BankAccountNotFoundException
                Arguments.of(new BigDecimal("50.00"), Optional.empty(), BankAccountNotFoundException.class),

                // Negative amount - should throw IllegalArgumentException
                Arguments.of(new BigDecimal("-50.00"), Optional.of(testAccount), IllegalArgumentException.class),

                // Zero amount - should throw IllegalArgumentException
                Arguments.of(BigDecimal.ZERO, Optional.of(testAccount), IllegalArgumentException.class)
        );
    }

    private static Stream<Arguments> withdrawInvalidScenariosProvider() {
        Account testAccount = new Account(UUID.randomUUID(), new BigDecimal("100.00"), "EUR", OffsetDateTime.now());
        return Stream.of(
                // Account not found - should throw BankAccountNotFoundException
                Arguments.of(new BigDecimal("30.00"), Optional.empty(), BankAccountNotFoundException.class),

                // Negative amount - should throw IllegalArgumentException
                Arguments.of(new BigDecimal("-30.00"), Optional.of(testAccount), IllegalArgumentException.class),

                // Insufficient funds (withdraw 200 from 100) - should throw IllegalArgumentException
                Arguments.of(new BigDecimal("200.00"), Optional.of(testAccount), IllegalArgumentException.class)
        );
    }

    // ========================================
    // CREATE ACCOUNT TESTS
    // ========================================

    @ParameterizedTest(name = "Create account with currency: {0} (expected: {1})")
    @CsvSource({
            ",EUR",           // null → EUR (default)
            "EUR,EUR",
            "USD,USD",
            "GBP,GBP"
    })
    void test_createAccount_should_create_account_with_currency(String inputCurrency, String expectedCurrency) {
        // Given
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        Account savedAccount = new Account(
                UUID.randomUUID(),
                BigDecimal.ZERO,
                expectedCurrency,
                OffsetDateTime.now()
        );

        // When
        when(accountPort.save(accountCaptor.capture())).thenReturn(savedAccount);
        UUID accountId = accountService.createAccount(inputCurrency);

        // Then
        assertNotNull(accountId);
        assertEquals(savedAccount.getAccountId(), accountId);

        // Verify that Account.create() created an account with expected currency
        Account capturedAccount = accountCaptor.getValue();
        assertEquals(expectedCurrency, capturedAccount.getCurrency());
        assertEquals(BigDecimal.ZERO, capturedAccount.getBalance());
        assertNotNull(capturedAccount.getAccountId());
        assertNotNull(capturedAccount.getCreatedAt());

        verify(accountPort).save(any(Account.class));
    }

    // ========================================
    // GET ACCOUNT DETAILS TESTS
    // ========================================

    @Test
    void test_getAccountDetails_should_return_account_when_exists() {
        // Given / When
        when(accountPort.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        Account foundAccount = accountService.getAccountDetails(testAccountId);

        // Then
        assertNotNull(foundAccount);
        assertEquals(testAccountId, foundAccount.getAccountId());
        assertEquals(new BigDecimal("100.00"), foundAccount.getBalance());
        assertEquals("EUR", foundAccount.getCurrency());
        verify(accountPort).findById(testAccountId);
    }

    @Test
    void test_getAccountDetails_should_throw_exception_when_not_found() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        when(accountPort.findById(nonExistentId)).thenReturn(Optional.empty());

        // Then
        BankAccountNotFoundException exception = assertThrows(
                BankAccountNotFoundException.class,
                () -> accountService.getAccountDetails(nonExistentId)
        );
        assertTrue(exception.getMessage().contains("Bank account not found"));
        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
        verify(accountPort).findById(nonExistentId);
    }

    // ========================================
    // GET BALANCE TESTS
    // ========================================

    @Test
    void test_getBalance_should_return_account_balance() {
        // Given / When
        when(accountPort.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        BigDecimal balance = accountService.getBalance(testAccountId);

        // Then
        assertNotNull(balance);
        assertEquals(new BigDecimal("100.00"), balance);
        verify(accountPort).findById(testAccountId);
    }

    @Test
    void test_getBalance_should_throw_exception_when_account_not_found() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        when(accountPort.findById(nonExistentId)).thenReturn(Optional.empty());

        // Then
        assertThrows(
                BankAccountNotFoundException.class,
                () -> accountService.getBalance(nonExistentId)
        );
        verify(accountPort).findById(nonExistentId);
    }

    // ========================================
    // DEPOSIT TESTS
    // ========================================

    @Test
    void test_deposit_should_add_amount_to_account() {
        // Given
        BigDecimal depositAmount = new BigDecimal("50.00");
        Account updatedAccount = new Account(
                testAccountId,
                new BigDecimal("150.00"),
                "EUR",
                testAccount.getCreatedAt()
        );

        // When
        when(accountPort.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountPort.save(any(Account.class))).thenReturn(updatedAccount);
        accountService.deposit(testAccountId, depositAmount);

        // Then
        verify(accountPort).findById(testAccountId);
        verify(accountPort).save(any(Account.class));
    }

    @ParameterizedTest(name = "Deposit should throw {2}")
    @MethodSource("depositInvalidScenariosProvider")
    void test_deposit_should_throw_exception_for_invalid_scenarios(
            BigDecimal depositAmount,
            Optional<Account> accountOptional,
            Class<? extends Exception> expectedException) {
        // Given
        UUID accountId = UUID.randomUUID();
        when(accountPort.findById(accountId)).thenReturn(accountOptional);

        // When / Then
        assertThrows(expectedException, () -> accountService.deposit(accountId, depositAmount));
        verify(accountPort).findById(accountId);
        verify(accountPort, never()).save(any(Account.class));
    }

    // ========================================
    // WITHDRAW TESTS
    // ========================================

    @Test
    void test_withdraw_should_subtract_amount_from_account() {
        // Given
        BigDecimal withdrawAmount = new BigDecimal("30.00");
        Account updatedAccount = new Account(
                testAccountId,
                new BigDecimal("70.00"),
                "EUR",
                testAccount.getCreatedAt()
        );

        // When
        when(accountPort.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountPort.save(any(Account.class))).thenReturn(updatedAccount);
        accountService.withdraw(testAccountId, withdrawAmount);

        // Then
        verify(accountPort).findById(testAccountId);
        verify(accountPort).save(any(Account.class));
    }

    @ParameterizedTest(name = "Withdraw should throw {2}")
    @MethodSource("withdrawInvalidScenariosProvider")
    void test_withdraw_should_throw_exception_for_invalid_scenarios(
            BigDecimal amount,
            Optional<Account> accountOptional,
            Class<? extends Exception> expectedException) {
        // Given
        UUID accountId = UUID.randomUUID();
        when(accountPort.findById(accountId)).thenReturn(accountOptional);

        // When / Then
        assertThrows(expectedException, () -> accountService.withdraw(accountId, amount));
        verify(accountPort).findById(accountId);
        verify(accountPort, never()).save(any(Account.class));
    }

    // ========================================
    // INTEGRATION SCENARIO TESTS
    // ========================================

    @Test
    void test_complete_scenario_create_deposit_withdraw_getBalance() {
        // Given
        String currency = "USD";
        UUID accountId = UUID.randomUUID();
        Account createdAccount = new Account(accountId, BigDecimal.ZERO, currency, OffsetDateTime.now());

        // When - Create
        when(accountPort.save(any(Account.class))).thenReturn(createdAccount);
        UUID createdId = accountService.createAccount(currency);

        // Then - Create
        assertNotNull(createdId);
        verify(accountPort).save(any(Account.class));

        // When - Deposit
        Account accountAfterDeposit = new Account(accountId, new BigDecimal("100.00"), currency, createdAccount.getCreatedAt());
        when(accountPort.findById(accountId)).thenReturn(Optional.of(createdAccount));
        when(accountPort.save(any(Account.class))).thenReturn(accountAfterDeposit);
        accountService.deposit(accountId, new BigDecimal("100.00"));

        // Then - Deposit
        verify(accountPort, times(2)).save(any(Account.class));

        // When - Get Balance
        when(accountPort.findById(accountId)).thenReturn(Optional.of(accountAfterDeposit));
        BigDecimal balance = accountService.getBalance(accountId);

        // Then - Get Balance
        assertEquals(new BigDecimal("100.00"), balance);
    }
}
