package com.bank.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountStatement record.
 */
class AccountStatementTest {

    private static final String CURRENCY_EUR = "EUR";
    private static final UUID TEST_ACCOUNT_ID = UUID.randomUUID();
    private static final BigDecimal TEST_BALANCE = new BigDecimal("1000.00");
    private static final LocalDate TEST_START_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDate TEST_END_DATE = LocalDate.of(2026, 1, 31);

    // ========================================
    // METHOD SOURCE FOR PARAMETERIZED TESTS
    // ========================================

    private static Stream<Arguments> nullFieldValidationProvider() {
        return Stream.of(
                Arguments.of(null, AccountType.CURRENT, TEST_BALANCE, CURRENCY_EUR, TEST_START_DATE, TEST_END_DATE, Page.empty(), "accountId null"),
                Arguments.of(TEST_ACCOUNT_ID, null, TEST_BALANCE, CURRENCY_EUR, TEST_START_DATE, TEST_END_DATE, Page.empty(), "accountType null"),
                Arguments.of(TEST_ACCOUNT_ID, AccountType.CURRENT, null, CURRENCY_EUR, TEST_START_DATE, TEST_END_DATE, Page.empty(), "balanceAtEndDate null"),
                Arguments.of(TEST_ACCOUNT_ID, AccountType.CURRENT, TEST_BALANCE, CURRENCY_EUR, null, TEST_END_DATE, Page.empty(), "periodStart null"),
                Arguments.of(TEST_ACCOUNT_ID, AccountType.CURRENT, TEST_BALANCE, CURRENCY_EUR, TEST_START_DATE, null, Page.empty(), "periodEnd null"),
                Arguments.of(TEST_ACCOUNT_ID, AccountType.CURRENT, TEST_BALANCE, CURRENCY_EUR, TEST_START_DATE, TEST_END_DATE, null, "operations null")
        );
    }

    // ========================================
    // CONSTRUCTOR TESTS
    // ========================================

    @Test
    void constructor_shouldCreateAccountStatement() {
        // Given
        Page<Operation> operations = Page.empty();

        // When
        AccountStatement statement = new AccountStatement(
                TEST_ACCOUNT_ID,
                AccountType.CURRENT,
                TEST_BALANCE,
                CURRENCY_EUR,
                TEST_START_DATE,
                TEST_END_DATE,
                operations
        );

        // Then
        assertNotNull(statement);
        assertEquals(TEST_ACCOUNT_ID, statement.accountId());
        assertEquals(AccountType.CURRENT, statement.accountType());
        assertEquals(TEST_BALANCE, statement.balanceAtEndDate());
        assertEquals(CURRENCY_EUR, statement.currency());
        assertEquals(TEST_START_DATE, statement.periodStart());
        assertEquals(TEST_END_DATE, statement.periodEnd());
        assertEquals(operations, statement.operations());
    }

    @ParameterizedTest(name = "Should throw NullPointerException when {7}")
    @MethodSource("nullFieldValidationProvider")
    void constructor_shouldThrowException_whenFieldIsNull(
            UUID accountId,
            AccountType accountType,
            BigDecimal balanceAtEndDate,
            String currency,
            LocalDate periodStart,
            LocalDate periodEnd,
            Page<Operation> operations,
            String testCase
    ) {
        // When & Then
        assertThrows(NullPointerException.class, () ->
                new AccountStatement(
                        accountId,
                        accountType,
                        balanceAtEndDate,
                        currency,
                        periodStart,
                        periodEnd,
                        operations
                )
        );
    }

    @Test
    void constructor_shouldThrowException_whenPeriodStartIsAfterPeriodEnd() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 31);
        LocalDate endDate = LocalDate.of(2026, 1, 1);

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new AccountStatement(
                        TEST_ACCOUNT_ID,
                        AccountType.CURRENT,
                        TEST_BALANCE,
                        CURRENCY_EUR,
                        startDate,
                        endDate,
                        Page.empty()
                )
        );
    }

    @Test
    void constructor_shouldAccept_whenPeriodStartEqualsEnd() {
        // Given
        LocalDate sameDate = LocalDate.of(2026, 1, 15);

        // When
        AccountStatement statement = new AccountStatement(
                TEST_ACCOUNT_ID,
                AccountType.CURRENT,
                TEST_BALANCE,
                CURRENCY_EUR,
                sameDate,
                sameDate,
                Page.empty()
        );

        // Then
        assertNotNull(statement);
        assertEquals(sameDate, statement.periodStart());
        assertEquals(sameDate, statement.periodEnd());
    }

    @Test
    void equals_shouldReturnTrue_whenStatementsAreEqual() {
        // Given
        Page<Operation> operations = Page.empty();

        AccountStatement statement1 = new AccountStatement(
                TEST_ACCOUNT_ID,
                AccountType.CURRENT,
                TEST_BALANCE,
                CURRENCY_EUR,
                TEST_START_DATE,
                TEST_END_DATE,
                operations
        );

        AccountStatement statement2 = new AccountStatement(
                TEST_ACCOUNT_ID,
                AccountType.CURRENT,
                TEST_BALANCE,
                CURRENCY_EUR,
                TEST_START_DATE,
                TEST_END_DATE,
                operations
        );

        // When & Then
        assertEquals(statement1, statement2);
        assertEquals(statement1.hashCode(), statement2.hashCode());
    }
}
