package com.bank.domain.service;

import com.bank.domain.config.BankAccountProperties;
import com.bank.domain.exception.BankAccountNotFoundException;
import com.bank.domain.model.*;
import com.bank.domain.port.out.AccountPort;
import com.bank.domain.port.out.OperationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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

    @Mock
    private OperationPort operationPort;

    @Mock
    private BankAccountProperties bankAccountProperties;

    @InjectMocks
    private AccountService accountService;

    private UUID testAccountId;
    private Account testAccount;

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
    }

    // ========================================
    // METHOD SOURCES FOR PARAMETERIZED TESTS
    // ========================================

    private static Stream<Arguments> depositInvalidScenariosProvider() {
        Account testAccount = new CurrentAccount(UUID.randomUUID(), new BigDecimal("100.00"), "EUR", OffsetDateTime.now(), AccountType.CURRENT, null);
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
        Account testAccount = new CurrentAccount(UUID.randomUUID(), new BigDecimal("100.00"), "EUR", OffsetDateTime.now(), AccountType.CURRENT, null);
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
            "'',EUR",         // empty string → EUR (default)
            "'   ',EUR",      // blank string → EUR (default)
            "EUR,EUR",
            "USD,USD",
            "GBP,GBP"
    })
    void test_createAccount_should_create_account_with_currency(String inputCurrency, String expectedCurrency) {
        // Given
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        CurrentAccount savedAccount = new CurrentAccount(
                UUID.randomUUID(),
                BigDecimal.ZERO,
                expectedCurrency,
                OffsetDateTime.now(),
                AccountType.CURRENT,
                null
        );

        // When
        when(accountPort.save(accountCaptor.capture())).thenReturn(savedAccount);
        Account createdAccount = accountService.createAccount(AccountType.CURRENT, inputCurrency);

        // Then
        assertNotNull(createdAccount);
        assertEquals(savedAccount.getAccountId(), createdAccount.getAccountId());
        assertEquals(savedAccount.getBalance(), createdAccount.getBalance());
        assertEquals(savedAccount.getCurrency(), createdAccount.getCurrency());

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
        CurrentAccount updatedAccount = new CurrentAccount(
                testAccountId,
                new BigDecimal("150.00"),
                "EUR",
                testAccount.getCreatedAt(),
                AccountType.CURRENT,
                null
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
        CurrentAccount updatedAccount = new CurrentAccount(
                testAccountId,
                new BigDecimal("70.00"),
                "EUR",
                testAccount.getCreatedAt(),
                AccountType.CURRENT,
                null
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
        CurrentAccount createdAccount = new CurrentAccount(accountId, BigDecimal.ZERO, currency, OffsetDateTime.now(), AccountType.CURRENT, null);

        // When - Create
        when(accountPort.save(any(Account.class))).thenReturn(createdAccount);
        Account createdId = accountService.createAccount(AccountType.CURRENT, currency);

        // Then - Create
        assertNotNull(createdId);
        verify(accountPort).save(any(Account.class));

        // When - Deposit
        CurrentAccount accountAfterDeposit = new CurrentAccount(accountId, new BigDecimal("100.00"), currency, createdAccount.getCreatedAt(), AccountType.CURRENT, null);
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

    // ========================================
    // SET OVERDRAFT LIMIT TESTS
    // ========================================

    @Test
    void test_setOverdraftLimit_should_update_account_overdraft() {
        // Given
        BigDecimal newOverdraftLimit = new BigDecimal("200.00");
        when(accountPort.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountPort.save(any(Account.class))).thenReturn(testAccount);

        // When
        accountService.setOverdraftLimit(testAccountId, newOverdraftLimit);

        // Then
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountPort).findById(testAccountId);
        verify(accountPort).save(accountCaptor.capture());

        Account savedAccount = accountCaptor.getValue();
        assertTrue(savedAccount instanceof CurrentAccount);
        assertEquals(newOverdraftLimit, ((CurrentAccount) savedAccount).getOverdraftLimit());
    }

    @Test
    void test_setOverdraftLimit_should_allow_null_to_remove_overdraft() {
        // Given
        CurrentAccount accountWithOverdraft = new CurrentAccount(
                testAccountId,
                new BigDecimal("100.00"),
                "EUR",
                OffsetDateTime.now(),
                AccountType.CURRENT,
                new BigDecimal("100.00")
        );
        when(accountPort.findById(testAccountId)).thenReturn(Optional.of(accountWithOverdraft));
        when(accountPort.save(any(Account.class))).thenReturn(accountWithOverdraft);

        // When
        accountService.setOverdraftLimit(testAccountId, null);

        // Then
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountPort).save(accountCaptor.capture());

        Account savedAccount = accountCaptor.getValue();
        assertTrue(savedAccount instanceof CurrentAccount);
        assertNull(((CurrentAccount) savedAccount).getOverdraftLimit());
    }

    @Test
    void test_setOverdraftLimit_should_allow_zero() {
        // Given
        when(accountPort.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountPort.save(any(Account.class))).thenReturn(testAccount);

        // When
        accountService.setOverdraftLimit(testAccountId, BigDecimal.ZERO);

        // Then
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountPort).save(accountCaptor.capture());

        Account savedAccount = accountCaptor.getValue();
        assertTrue(savedAccount instanceof CurrentAccount);
        assertEquals(BigDecimal.ZERO, ((CurrentAccount) savedAccount).getOverdraftLimit());
    }

    @Test
    void test_setOverdraftLimit_should_throw_exception_when_account_not_found() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(accountPort.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                BankAccountNotFoundException.class,
                () -> accountService.setOverdraftLimit(nonExistentId, new BigDecimal("100.00"))
        );

        verify(accountPort).findById(nonExistentId);
        verify(accountPort, never()).save(any(Account.class));
    }

    @Test
    void test_setOverdraftLimit_should_throw_exception_when_negative() {
        // Given
        BigDecimal negativeOverdraft = new BigDecimal("-100.00");

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.setOverdraftLimit(testAccountId, negativeOverdraft)
        );

        assertEquals("Overdraft limit must be positive or zero", exception.getMessage());

        // Note: Validation fails BEFORE calling getAccountDetails()
        // therefore neither findById() nor save() are called
        verify(accountPort, never()).findById(any(UUID.class));
        verify(accountPort, never()).save(any(Account.class));
    }

    @ParameterizedTest(name = "Set overdraft limit to {0}")
    @CsvSource({
            "0.00,    zero overdraft",
            "50.00,   small overdraft",
            "100.00,  standard overdraft",
            "500.00,  large overdraft",
            "1000.00, premium overdraft"
    })
    void test_setOverdraftLimit_should_accept_various_valid_amounts(String overdraftAmount, String description) {
        // Given
        BigDecimal overdraftLimit = new BigDecimal(overdraftAmount);
        when(accountPort.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(accountPort.save(any(Account.class))).thenReturn(testAccount);

        // When
        accountService.setOverdraftLimit(testAccountId, overdraftLimit);

        // Then
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountPort).save(accountCaptor.capture());

        Account savedAccount = accountCaptor.getValue();
        assertTrue(savedAccount instanceof CurrentAccount);
        assertEquals(overdraftLimit, ((CurrentAccount) savedAccount).getOverdraftLimit());
    }

    // ========================================
    // TESTS FOR GET ACCOUNT STATEMENT
    // ========================================

    @Test
    void getAccountStatement_shouldReturnStatement_whenValidPeriod() {
        // Given
        Account account = CurrentAccount.create("EUR");
        UUID accountId = account.getAccountId();
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        Operation operation1 = Operation.create(accountId, OperationType.DEPOSIT, new BigDecimal("100"), new BigDecimal("1100"));
        Operation operation2 = Operation.create(accountId, OperationType.WITHDRAW, new BigDecimal("50"), new BigDecimal("1050"));
        Page<Operation> operations = new PageImpl<>(List.of(operation1, operation2));

        when(accountPort.findById(accountId)).thenReturn(Optional.of(account));
        when(operationPort.findByAccountIdAndPeriod(accountId, startDate, endDate, 0, 20))
                .thenReturn(operations);

        // When
        AccountStatement result = accountService.getAccountStatement(accountId, startDate, endDate, 0, 20);

        // Then
        assertNotNull(result);
        assertEquals(accountId, result.accountId());
        assertEquals(AccountType.CURRENT, result.accountType());
        assertEquals(startDate, result.periodStart());
        assertEquals(endDate, result.periodEnd());
        assertEquals(2, result.operations().getTotalElements());
        verify(accountPort).findById(accountId);
        verify(operationPort).findByAccountIdAndPeriod(accountId, startDate, endDate, 0, 20);
    }

    @Test
    void getAccountStatement_shouldUseDefaultPeriod_whenDatesAreNull() {
        // Given
        UUID accountId = UUID.randomUUID();
        Account account = CurrentAccount.create("EUR");
        Page<Operation> operations = Page.empty();

        when(accountPort.findById(accountId)).thenReturn(Optional.of(account));
        when(operationPort.findByAccountIdAndPeriod(eq(accountId), any(LocalDate.class), any(LocalDate.class), eq(0), eq(20)))
                .thenReturn(operations);

        // When
        AccountStatement result = accountService.getAccountStatement(accountId, null, null, 0, 20);

        // Then
        assertNotNull(result);
        assertNotNull(result.periodStart());
        assertNotNull(result.periodEnd());
        verify(operationPort).findByAccountIdAndPeriod(eq(accountId), any(LocalDate.class), any(LocalDate.class), eq(0), eq(20));
    }

    @Test
    void getAccountStatement_shouldThrowException_whenAccountNotFound() {
        // Given
        UUID accountId = UUID.randomUUID();
        when(accountPort.findById(accountId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BankAccountNotFoundException.class, () ->
                accountService.getAccountStatement(accountId, LocalDate.now(), LocalDate.now(), 0, 20)
        );
    }

    @Test
    void getAccountStatement_shouldThrowException_whenStartDateAfterEndDate() {
        // Given
        UUID accountId = UUID.randomUUID();
        Account account = CurrentAccount.create("EUR");
        LocalDate startDate = LocalDate.of(2026, 1, 31);
        LocalDate endDate = LocalDate.of(2026, 1, 1);

        when(accountPort.findById(accountId)).thenReturn(Optional.of(account));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                accountService.getAccountStatement(accountId, startDate, endDate, 0, 20)
        );
    }

    @Test
    void getAccountStatement_shouldCalculateBalanceFromOperations_whenPeriodIsInPast() {
        // Given
        UUID accountId = UUID.randomUUID();
        Account account = CurrentAccount.create("EUR");
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        Operation lastOperation = Operation.create(accountId, OperationType.DEPOSIT, new BigDecimal("100"), new BigDecimal("1500"));
        Page<Operation> operations = new PageImpl<>(List.of(lastOperation));

        when(accountPort.findById(accountId)).thenReturn(Optional.of(account));
        when(operationPort.findByAccountIdAndPeriod(accountId, startDate, endDate, 0, 20))
                .thenReturn(operations);

        // When
        AccountStatement result = accountService.getAccountStatement(accountId, startDate, endDate, 0, 20);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("1500"), result.balanceAtEndDate()); // Balance from last operation
    }

    @Test
    void getAccountStatement_shouldReturnCurrentBalance_whenNoOperationsInPeriod() {
        // Given
        UUID accountId = UUID.randomUUID();
        CurrentAccount account = CurrentAccount.create("EUR");
        account.deposit(new BigDecimal("1000"));
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        Page<Operation> operations = Page.empty();

        when(accountPort.findById(accountId)).thenReturn(Optional.of(account));
        when(operationPort.findByAccountIdAndPeriod(accountId, startDate, endDate, 0, 20))
                .thenReturn(operations);

        // When
        AccountStatement result = accountService.getAccountStatement(accountId, startDate, endDate, 0, 20);

        // Then
        assertNotNull(result);
        assertEquals(account.getBalance(), result.balanceAtEndDate());
    }

    @Test
    void getAccountStatement_shouldWorkForSavingsAccount() {
        // Given
        UUID accountId = UUID.randomUUID();
        Account account = SavingsAccount.create("EUR", new BigDecimal("3000"));
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);
        Page<Operation> operations = Page.empty();

        when(accountPort.findById(accountId)).thenReturn(Optional.of(account));
        when(operationPort.findByAccountIdAndPeriod(accountId, startDate, endDate, 0, 20))
                .thenReturn(operations);

        // When
        AccountStatement result = accountService.getAccountStatement(accountId, startDate, endDate, 0, 20);

        // Then
        assertNotNull(result);
        assertEquals(AccountType.SAVINGS, result.accountType());
    }
}
