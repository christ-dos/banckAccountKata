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
 * Unit tests for Account domain model.
 */
class AccountTest {

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
        Account account = Account.create(inputCurrency);

        // Then
        assertNotNull(account.getAccountId());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals(expectedCurrency, account.getCurrency());
        assertNotNull(account.getCreatedAt());
    }

    @Test
    void test_account_should_be_created_with_specific_balance() {
        // Given & When
        Account account = new Account(
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "EUR",
                OffsetDateTime.now()
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
        Account account = Account.create("EUR");

        // When
        account.deposit(new BigDecimal(depositAmount));

        // Then
        assertEquals(new BigDecimal(expectedBalance), account.getBalance());
    }

    @Test
    void test_deposit_should_handle_multiple_deposits() {
        // Given
        Account account = Account.create("EUR");

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
        Account account = Account.create("EUR");
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
        Account account = new Account(
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "EUR",
                OffsetDateTime.now()
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
        Account account = Account.create("EUR");
        account.deposit(new BigDecimal("100.00"));

        // When
        account.withdraw(new BigDecimal(withdrawAmount));

        // Then
        assertEquals(new BigDecimal(expectedBalance), account.getBalance());
    }

    @Test
    void test_withdraw_should_allow_withdrawing_all_balance() {
        // Given
        Account account = Account.create("EUR");
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
        Account account = Account.create("EUR");
        account.deposit(new BigDecimal("100.00"));
        BigDecimal amount = amountStr != null ? new BigDecimal(amountStr) : null;

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> account.withdraw(amount)
        );
        assertEquals("Withdrawal amount must be positive and not null", exception.getMessage());
    }

    @Test
    void test_withdraw_should_throw_exception_when_insufficient_funds() {
        // Given
        Account account = Account.create("EUR");
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
        Account account = new Account(
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "EUR",
                OffsetDateTime.now()
        );

        // When
        account.withdraw(new BigDecimal("30.00"));

        // Then
        assertEquals(new BigDecimal("70.00"), account.getBalance());
    }

    @Test
    void test_withdraw_should_fail_with_existing_balance_insufficient() {
        // Given
        Account account = new Account(
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                "EUR",
                OffsetDateTime.now()
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
        Account account = Account.create("EUR");

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
        Account account = new Account(
                UUID.randomUUID(),
                new BigDecimal("1000000.00"),
                "EUR",
                OffsetDateTime.now()
        );

        // When
        account.deposit(new BigDecimal("500000.00"));
        account.withdraw(new BigDecimal("200000.00"));

        // Then
        assertEquals(new BigDecimal("1300000.00"), account.getBalance());
    }
}
