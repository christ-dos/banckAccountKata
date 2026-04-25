package com.bank.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CurrentAccount domain model.
 */
class CurrentAccountTest {


    // ========================================
    // ACCOUNT CREATION TESTS
    // ========================================

    @ParameterizedTest(name = "Create account with currency: {0} -> expected: {1}")
    @CsvSource({
            ",           EUR,     default currency when null",
            "EUR,       EUR,     explicit EUR currency",
            "USD,       USD,     US dollar currency",
            "GBP,       GBP,     British pound currency",
            "JPY,       JPY,     Japanese yen currency"
    })
    void test_create_should_create_account_with_currency(String inputCurrency, String expectedCurrency, String description) {
        // When
        CurrentAccount account = CurrentAccount.create(inputCurrency);

        // Then
        assertNotNull(account.getAccountId());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals(expectedCurrency, account.getCurrency());
        assertNotNull(account.getCreatedAt());
    }

    @Test
    void test_account_should_be_created_with_specific_balance() {
        // Given & When
        CurrentAccount account = new CurrentAccount(
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "EUR",
                OffsetDateTime.now(),
                AccountType.CURRENT,
                null
        );

        // Then
        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    // ========================================
    // DEPOSIT TESTS
    // ========================================

    @ParameterizedTest(name = "Deposit {0} -> balance should be {1}")
    @CsvSource({
            "100.00,     100.00,     single deposit",
            "50.50,      50.50,      deposit with cents",
            "0.01,       0.01,       minimal deposit amount",
            "999999.99,  999999.99,  large deposit amount"
    })
    void test_deposit_should_add_money_successfully(String depositAmount, String expectedBalance, String description) {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR");

        // When
        account.deposit(new BigDecimal(depositAmount));

        // Then
        assertEquals(new BigDecimal(expectedBalance), account.getBalance());
    }

    @Test
    void test_deposit_should_handle_multiple_deposits() {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR");

        // When
        account.deposit(new BigDecimal("50.00"));
        account.deposit(new BigDecimal("30.00"));
        account.deposit(new BigDecimal("20.00"));

        // Then
        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    @ParameterizedTest(name = "Deposit with invalid amount: {0}")
    @NullSource
    @CsvSource({
            "0,           zero amount",
            "-50.00,      negative amount",
            "-0.01,       small negative amount"
    })
    void test_deposit_should_throw_exception_when_amount_is_invalid(String amountStr) {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR");
        BigDecimal amount = amountStr != null ? new BigDecimal(amountStr) : null;

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.deposit(amount)
        );
        assertEquals("Deposit amount must be positive and not null", exception.getMessage());
    }

    @Test
    void test_deposit_should_add_to_existing_balance() {
        // Given
        CurrentAccount account = new CurrentAccount(
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "EUR",
                OffsetDateTime.now(),
                AccountType.CURRENT,
                null
        );

        // When
        account.deposit(new BigDecimal("50.00"));

        // Then
        assertEquals(new BigDecimal("150.00"), account.getBalance());
    }

    // ========================================
    // WITHDRAWAL TESTS
    // ========================================

    @ParameterizedTest(name = "Withdraw {0} from 100.00 -> balance should be {1}")
    @CsvSource({
            "30.00,      70.00,      standard withdrawal",
            "0.01,       99.99,      minimal withdrawal amount",
            "99.99,      0.01,       almost all balance",
            "50.50,      49.50,      withdrawal with cents"
    })
    void test_withdraw_should_subtract_money_successfully(String withdrawAmount, String expectedBalance, String description) {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR");
        account.deposit(new BigDecimal("100.00"));

        // When
        account.withdraw(new BigDecimal(withdrawAmount));

        // Then
        assertEquals(new BigDecimal(expectedBalance), account.getBalance());
    }

    @Test
    void test_withdraw_should_allow_withdrawing_all_balance() {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR");
        account.deposit(new BigDecimal("100.00"));

        // When
        account.withdraw(new BigDecimal("100.00"));

        // Then
        assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO));
    }

    @ParameterizedTest(name = "Withdraw with invalid amount: {0}")
    @NullSource
    @CsvSource({
            "0,           zero amount",
            "-50.00,      negative amount",
            "-0.01,       small negative amount"
    })
    void test_withdraw_should_throw_exception_when_amount_is_invalid(String amountStr) {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR");
        account.deposit(new BigDecimal("100.00"));
        BigDecimal amount = amountStr != null ? new BigDecimal(amountStr) : null;

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(amount)
        );
        assertEquals("Amount must be positive and not null", exception.getMessage());
    }

    @Test
    void test_withdraw_should_throw_exception_when_insufficient_funds() {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR");
        account.deposit(new BigDecimal("50.00"));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(new BigDecimal("100.00"))
        );
        assertTrue(exception.getMessage().contains("Insufficient funds"));
    }

    @Test
    void test_withdraw_should_subtract_from_existing_balance() {
        // Given
        CurrentAccount account = new CurrentAccount(
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "EUR",
                OffsetDateTime.now(),
                AccountType.CURRENT,
                null
        );

        // When
        account.withdraw(new BigDecimal("30.00"));

        // Then
        assertEquals(new BigDecimal("70.00"), account.getBalance());
    }

    @Test
    void test_withdraw_should_fail_with_existing_balance_insufficient() {
        // Given
        CurrentAccount account = new CurrentAccount(
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                "EUR",
                OffsetDateTime.now(),
                AccountType.CURRENT,
                null
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(new BigDecimal("100.00"))
        );
        assertTrue(exception.getMessage().contains("Insufficient funds"));
        assertEquals(new BigDecimal("50.00"), account.getBalance()); // Balance unchanged
    }

    // ========================================
    // COMBINED SCENARIOS TESTS
    // ========================================

    @Test
    void test_account_should_handle_multiple_deposits_and_withdrawals() {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR");

        // When
        account.deposit(new BigDecimal("100.00"));
        account.withdraw(new BigDecimal("30.00"));
        account.deposit(new BigDecimal("50.00"));
        account.withdraw(new BigDecimal("20.00"));

        // Then
        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    @Test
    void test_account_should_handle_large_balance() {
        // Given
        CurrentAccount account = new CurrentAccount(
                UUID.randomUUID(),
                new BigDecimal("1000000.00"),
                "EUR",
                OffsetDateTime.now(),
                AccountType.CURRENT,
                null
        );

        // When
        account.deposit(new BigDecimal("500000.00"));
        account.withdraw(new BigDecimal("200000.00"));

        // Then
        assertEquals(new BigDecimal("1300000.00"), account.getBalance());
    }

    // ========================================
    // OVERDRAFT LIMIT TESTS
    // ========================================

    @Test
    void test_create_account_with_overdraft_limit() {
        // When
        CurrentAccount account = CurrentAccount.create("EUR", new BigDecimal("100.00"));

        // Then
        assertNotNull(account.getAccountId());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals("EUR", account.getCurrency());
        assertEquals(new BigDecimal("100.00"), account.getOverdraftLimit());
    }

    @Test
    void test_create_account_without_overdraft_limit() {
        // When
        CurrentAccount account = CurrentAccount.create("EUR");

        // Then
        assertNull(account.getOverdraftLimit());
    }

    @Test
    void test_create_account_should_reject_negative_overdraft() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CurrentAccount.create("EUR", new BigDecimal("-100.00"))
        );

        assertEquals("Overdraft limit must be positive", exception.getMessage());
    }


    @Test
    void test_withdraw_should_reject_exceeding_overdraft_limit() {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR", new BigDecimal("100.00"));
        account.deposit(new BigDecimal("50.00"));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(new BigDecimal("200.00"))
        );

        assertTrue(exception.getMessage().contains("Insufficient funds"));
        assertTrue(exception.getMessage().contains("would exceed overdraft limit of 100"));
    }

    @Test
    void test_withdraw_without_overdraft_should_not_allow_negative_balance() {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR"); // No overdraft
        account.deposit(new BigDecimal("50.00"));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(new BigDecimal("51.00"))
        );

        assertEquals("Insufficient funds for withdrawal: 51.00", exception.getMessage());
    }

    @Test
    void test_multiple_withdrawals_with_overdraft() {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR", new BigDecimal("200.00"));
        account.deposit(new BigDecimal("100.00"));

        // When
        account.withdraw(new BigDecimal("150.00")); // Balance: -50
        account.withdraw(new BigDecimal("100.00")); // Balance: -150

        // Then
        assertEquals(new BigDecimal("-150.00"), account.getBalance());
    }

    @ParameterizedTest(name = "Balance: {0}, Overdraft: {1}, Withdraw: {2} -> Expected balance: {3}")
    @CsvSource({
            "100.00,  null,    50.00,   50.00,    withdrawal without overdraft",
            "100.00,  100.00,  50.00,   50.00,    withdrawal with overdraft (not used)",
            "50.00,   100.00,  120.00,  -70.00,   withdrawal within overdraft limit",
            "50.00,   100.00,  150.00,  -100.00,  withdrawal to exact overdraft limit",
            "100.00,  100.00,  150.00,  -50.00,   standard overdraft usage",
            "100.00,  100.00,  200.00,  -100.00,  withdrawal to exact limit",
            "50.00,   200.00,  100.00,  -50.00,   large overdraft available",
            "0.00,    100.00,  50.00,   -50.00,   withdrawal from zero balance",
            "0.00,    100.00,  100.00,  -100.00,  max withdrawal from zero",
            "0.00,    50.00,   25.00,   -25.00,   partial overdraft from zero"
    })
    void test_withdraw_with_various_overdraft_scenarios(
            String initialBalance,
            String overdraftLimit,
            String withdrawAmount,
            String expectedBalance,
            String description
    ) {
        // Given
        BigDecimal overdraft = overdraftLimit != null && !overdraftLimit.equals("null")
                ? new BigDecimal(overdraftLimit)
                : null;
        CurrentAccount account = CurrentAccount.create("EUR", overdraft);

        // Deposit initial balance if not zero (to test overdraft from zero balance)
        BigDecimal balanceAmount = new BigDecimal(initialBalance);
        if (balanceAmount.signum() > 0) {
            account.deposit(balanceAmount);
        }

        // When
        account.withdraw(new BigDecimal(withdrawAmount));

        // Then
        assertEquals(new BigDecimal(expectedBalance), account.getBalance());
    }

    @ParameterizedTest(name = "Balance: {0}, Overdraft: {1}, Withdraw: {2} -> Should fail")
    @CsvSource({
            "100.00,  null,    150.00",
            "100.00,  100.00,  250.00",
            "50.00,   100.00,  200.00",
            "0.00,    50.00,   100.00",
            "0.00,    null,    1.00"
    })
    void test_withdraw_should_fail_when_exceeding_limits(
            String initialBalance,
            String overdraftLimit,
            String withdrawAmount
    ) {
        // Given
        BigDecimal overdraft = overdraftLimit != null && !overdraftLimit.equals("null")
                ? new BigDecimal(overdraftLimit)
                : null;
        CurrentAccount account = CurrentAccount.create("EUR", overdraft);

        // Deposit initial balance if not zero
        BigDecimal balanceAmount = new BigDecimal(initialBalance);
        if (balanceAmount.signum() > 0) {
            account.deposit(balanceAmount);
        }

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(new BigDecimal(withdrawAmount))
        );
    }

    @Test
    void test_deposit_and_withdraw_with_overdraft_should_work_correctly() {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR", new BigDecimal("500.00"));

        // When
        account.deposit(new BigDecimal("200.00"));   // Balance: 200
        account.withdraw(new BigDecimal("300.00"));  // Balance: -100
        account.deposit(new BigDecimal("150.00"));   // Balance: 50
        account.withdraw(new BigDecimal("100.00"));  // Balance: -50

        // Then
        assertEquals(new BigDecimal("-50.00"), account.getBalance());
    }

    @Test
    void test_overdraft_error_message_should_be_detailed() {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR", new BigDecimal("100.00"));
        account.deposit(new BigDecimal("50.00"));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(new BigDecimal("200.00"))
        );

        String message = exception.getMessage();
        assertTrue(message.contains("Insufficient funds"), "Message should mention insufficient funds");
        assertTrue(message.contains("100"), "Message should mention overdraft limit");
        assertTrue(message.contains("200"), "Message should mention withdrawal amount");
    }

    // ========================================
    // OVERDRAFT LIMIT MODIFICATION TESTS
    // ========================================

    @ParameterizedTest(name = "Set overdraft limit to {0} -> expected: {1}")
    @CsvSource({
            "200.00,  200.00,  set new overdraft limit",
            "100.00,  100.00,  set standard overdraft",
            "500.00,  500.00,  set large overdraft",
            "0.00,    0.00,    set overdraft to zero"
    })
    void test_setOverdraftLimit_should_update_limit(String overdraftValue, String expectedValue, String description) {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR");

        // When
        account.setOverdraftLimit(new BigDecimal(overdraftValue));

        // Then
        assertEquals(new BigDecimal(expectedValue), account.getOverdraftLimit());
    }

    @Test
    void test_setOverdraftLimit_should_allow_null_to_remove_overdraft() {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR", new BigDecimal("100.00"));

        // When
        account.setOverdraftLimit(null);

        // Then
        assertNull(account.getOverdraftLimit());
    }


    @Test
    void test_withdraw_should_respect_updated_overdraft_limit() {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR");
        account.deposit(new BigDecimal("50.00"));

        // When - Set overdraft and withdraw
        account.setOverdraftLimit(new BigDecimal("100.00"));
        account.withdraw(new BigDecimal("120.00"));

        // Then
        assertEquals(new BigDecimal("-70.00"), account.getBalance());
    }

    @Test
    void test_withdraw_should_fail_after_overdraft_removal() {
        // Given
        CurrentAccount account = CurrentAccount.create("EUR", new BigDecimal("100.00"));
        account.deposit(new BigDecimal("50.00"));

        // When - Remove overdraft
        account.setOverdraftLimit(null);

        // Then - Withdrawal should fail
        assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(new BigDecimal("60.00"))
        );
    }
}

