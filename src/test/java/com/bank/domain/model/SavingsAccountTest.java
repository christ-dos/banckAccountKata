package com.bank.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for SavingsAccount domain model.
 */
class SavingsAccountTest {

    private static final BigDecimal TEST_DEFAULT_DEPOSIT_LIMIT = BigDecimal.valueOf(22950);
    private static final String TEST_CURRENCY_EUR = "EUR";

    // ========================================
    // CREATION TESTS
    // ========================================

    @Test
    void test_create_with_default_limit_should_succeed() {
        // Given / When
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, TEST_DEFAULT_DEPOSIT_LIMIT);

        // Then
        assertThat(account).isNotNull();
        assertThat(account.getAccountId()).isNotNull();
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.getCurrency()).isEqualTo(TEST_CURRENCY_EUR);
        assertThat(account.getDepositLimit()).isEqualByComparingTo(TEST_DEFAULT_DEPOSIT_LIMIT);
        assertThat(account.getCreatedAt()).isNotNull();
    }

    @Test
    void test_create_with_custom_limit_should_succeed() {
        // Given
        BigDecimal customLimit = BigDecimal.valueOf(15000);

        // When
        SavingsAccount account = SavingsAccount.create("USD", customLimit);

        // Then
        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.getCurrency()).isEqualTo("USD");
        assertThat(account.getDepositLimit()).isEqualByComparingTo(customLimit);
    }

    @Test
    void test_create_with_null_currency_should_default_to_EUR() {
        // Given / When
        SavingsAccount account = SavingsAccount.create(null, TEST_DEFAULT_DEPOSIT_LIMIT);

        // Then
        assertThat(account.getCurrency()).isEqualTo(TEST_CURRENCY_EUR);
    }

    @Test
    void test_create_with_blank_currency_should_default_to_EUR() {
        // Given / When
        SavingsAccount account = SavingsAccount.create("  ", TEST_DEFAULT_DEPOSIT_LIMIT);

        // Then
        assertThat(account.getCurrency()).isEqualTo(TEST_CURRENCY_EUR);
    }

    @Test
    void test_create_with_null_deposit_limit_should_throw_exception() {
        // Given / When / Then
        assertThatThrownBy(() -> SavingsAccount.create(TEST_CURRENCY_EUR, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deposit limit must be positive");
    }

    @Test
    void test_create_with_zero_deposit_limit_should_throw_exception() {
        // Given / When / Then
        assertThatThrownBy(() -> SavingsAccount.create(TEST_CURRENCY_EUR, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deposit limit must be positive");
    }

    @Test
    void test_create_with_negative_deposit_limit_should_throw_exception() {
        // Given / When / Then
        assertThatThrownBy(() -> SavingsAccount.create(TEST_CURRENCY_EUR, BigDecimal.valueOf(-1000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deposit limit must be positive");
    }

    // ========================================
    // DEPOSIT TESTS
    // ========================================

    @ParameterizedTest(name = "Deposit {0}€ with limit {1}€ should succeed")
    @CsvSource({
            "100,   22950",  // Small deposit
            "22950, 22950",  // Exact limit
            "1000,  5000",   // Under custom limit
            "0.01,  22950"   // Minimum amount
    })
    void test_deposit_under_limit_should_succeed(String depositAmount, String depositLimit) {
        // Given
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, new BigDecimal(depositLimit));
        BigDecimal amount = new BigDecimal(depositAmount);

        // When
        account.deposit(amount);

        // Then
        assertThat(account.getBalance()).isEqualByComparingTo(amount);
    }

    @Test
    void test_deposit_multiple_times_under_limit_should_succeed() {
        // Given
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, BigDecimal.valueOf(1000));

        // When
        account.deposit(BigDecimal.valueOf(300));
        account.deposit(BigDecimal.valueOf(400));
        account.deposit(BigDecimal.valueOf(300));

        // Then
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void test_deposit_exceeding_limit_should_throw_exception() {
        // Given
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, BigDecimal.valueOf(1000));

        // When / Then
        assertThatThrownBy(() -> account.deposit(BigDecimal.valueOf(1500)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot deposit 1500")
                .hasMessageContaining("would exceed deposit limit of 1000")
                .hasMessageContaining("Maximum allowed deposit: 1000");
    }

    @Test
    void test_deposit_when_limit_already_reached_should_throw_exception() {
        // Given
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, BigDecimal.valueOf(1000));
        account.deposit(BigDecimal.valueOf(1000)); // Reach limit

        // When / Then
        assertThatThrownBy(() -> account.deposit(BigDecimal.valueOf(100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot deposit 100")
                .hasMessageContaining("deposit limit of 1000 already reached");
    }

    @Test
    void test_deposit_partial_amount_when_close_to_limit_should_fail_with_remaining_capacity() {
        // Given
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, BigDecimal.valueOf(1000));
        account.deposit(BigDecimal.valueOf(800)); // 200 remaining

        // When / Then
        assertThatThrownBy(() -> account.deposit(BigDecimal.valueOf(500)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Maximum allowed deposit: 200");
    }

    @ParameterizedTest(name = "Deposit invalid amount {0} should throw exception")
    @CsvSource({
            "null,  Deposit amount must be positive and not null",
            "0,     Deposit amount must be positive and not null",
            "-100,  Deposit amount must be positive and not null"
    })
    void test_deposit_invalid_amount_should_throw_exception(String amountStr, String expectedMessage) {
        // Given
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, TEST_DEFAULT_DEPOSIT_LIMIT);
        BigDecimal amount = "null".equals(amountStr) ? null : new BigDecimal(amountStr);

        // When / Then
        assertThatThrownBy(() -> account.deposit(amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    // ========================================
    // WITHDRAWAL TESTS
    // ========================================

    @ParameterizedTest(name = "Withdraw {0}€ from balance {1}€ should succeed")
    @CsvSource({
            "50,  100",   // Normal withdrawal
            "100, 100",   // Exact balance
            "1,   100",   // Small withdrawal
            "0.01, 1"     // Minimum amount
    })
    void test_withdraw_with_sufficient_balance_should_succeed(String withdrawAmount, String initialBalance) {
        // Given
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, BigDecimal.valueOf(10000));
        account.deposit(new BigDecimal(initialBalance));
        BigDecimal amount = new BigDecimal(withdrawAmount);

        // When
        account.withdraw(amount);

        // Then
        BigDecimal expectedBalance = new BigDecimal(initialBalance).subtract(amount);
        assertThat(account.getBalance()).isEqualByComparingTo(expectedBalance);
    }

    @Test
    void test_withdraw_exceeding_balance_should_throw_exception() {
        // Given
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, BigDecimal.valueOf(10000));
        account.deposit(BigDecimal.valueOf(100));

        // When / Then
        assertThatThrownBy(() -> account.withdraw(BigDecimal.valueOf(200)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient funds for withdrawal: 200");
    }

    @Test
    void test_withdraw_from_zero_balance_should_throw_exception() {
        // Given
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, TEST_DEFAULT_DEPOSIT_LIMIT);

        // When / Then
        assertThatThrownBy(() -> account.withdraw(BigDecimal.valueOf(50)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @ParameterizedTest(name = "Withdraw invalid amount {0} should throw exception")
    @CsvSource({
            "null,  Withdrawal amount must be positive and not null",
            "0,     Withdrawal amount must be positive and not null",
            "-100,  Withdrawal amount must be positive and not null"
    })
    void test_withdraw_invalid_amount_should_throw_exception(String amountStr, String expectedMessage) {
        // Given
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, BigDecimal.valueOf(10000));
        account.deposit(BigDecimal.valueOf(1000));
        BigDecimal amount = "null".equals(amountStr) ? null : new BigDecimal(amountStr);

        // When / Then
        assertThatThrownBy(() -> account.withdraw(amount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedMessage);
    }

    // ========================================
    // OVERDRAFT TESTS (Must be rejected)
    // ========================================

    @Test
    void test_setOverdraftLimit_should_throw_UnsupportedOperationException() {
        // Given
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, TEST_DEFAULT_DEPOSIT_LIMIT);

        // When / Then
        assertThatThrownBy(() -> account.setOverdraftLimit(BigDecimal.valueOf(500)))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Savings accounts cannot have overdraft authorization");
    }

    @Test
    void test_setOverdraftLimit_with_zero_should_also_throw_exception() {
        // Given
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, TEST_DEFAULT_DEPOSIT_LIMIT);

        // When / Then
        assertThatThrownBy(() -> account.setOverdraftLimit(BigDecimal.ZERO))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Savings accounts cannot have overdraft authorization");
    }

    // ========================================
    // COMPLEX SCENARIO TESTS
    // ========================================

    @Test
    void test_full_lifecycle_deposit_withdraw_under_limit() {
        // Given
        SavingsAccount account = SavingsAccount.create(TEST_CURRENCY_EUR, BigDecimal.valueOf(1000));

        // When - Deposit multiple times
        account.deposit(BigDecimal.valueOf(400));
        account.deposit(BigDecimal.valueOf(300));
        account.deposit(BigDecimal.valueOf(300)); // Total: 1000 (limit reached)

        // Then - Balance is at limit
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000));

        // When - Withdraw
        account.withdraw(BigDecimal.valueOf(200));

        // Then
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(800));

        // When - Can deposit again (200 available)
        account.deposit(BigDecimal.valueOf(200));

        // Then
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }
}


