package com.bank.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Operation record.
 */
class OperationTest {

    private static final UUID TEST_ACCOUNT_ID = UUID.randomUUID();
    private static final UUID TEST_OPERATION_ID = UUID.randomUUID();
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("100");
    private static final BigDecimal TEST_BALANCE_AFTER = new BigDecimal("1100");
    private static final OffsetDateTime TEST_DATE = OffsetDateTime.now();

    // ========================================
    // METHOD SOURCE FOR PARAMETERIZED TESTS
    // ========================================

    private static Stream<Arguments> nullFieldValidationProvider() {
        return Stream.of(
                Arguments.of(null, TEST_ACCOUNT_ID, OperationType.DEPOSIT, TEST_AMOUNT, TEST_BALANCE_AFTER, TEST_DATE, "operationId null"),
                Arguments.of(TEST_OPERATION_ID, null, OperationType.DEPOSIT, TEST_AMOUNT, TEST_BALANCE_AFTER, TEST_DATE, "accountId null"),
                Arguments.of(TEST_OPERATION_ID, TEST_ACCOUNT_ID, null, TEST_AMOUNT, TEST_BALANCE_AFTER, TEST_DATE, "type null"),
                Arguments.of(TEST_OPERATION_ID, TEST_ACCOUNT_ID, OperationType.DEPOSIT, null, TEST_BALANCE_AFTER, TEST_DATE, "amount null"),
                Arguments.of(TEST_OPERATION_ID, TEST_ACCOUNT_ID, OperationType.DEPOSIT, TEST_AMOUNT, null, TEST_DATE, "balanceAfter null"),
                Arguments.of(TEST_OPERATION_ID, TEST_ACCOUNT_ID, OperationType.DEPOSIT, TEST_AMOUNT, TEST_BALANCE_AFTER, null, "operationDate null")
        );
    }

    private static Stream<Arguments> invalidAmountProvider() {
        return Stream.of(
                Arguments.of(BigDecimal.ZERO, "amount is zero"),
                Arguments.of(new BigDecimal("-10"), "amount is negative"),
                Arguments.of(new BigDecimal("-100.50"), "amount is large negative")
        );
    }

    @Test
    void create_shouldCreateDepositOperation() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal balanceAfter = new BigDecimal("1100.00");

        // When
        Operation operation = Operation.create(TEST_ACCOUNT_ID, OperationType.DEPOSIT, amount, balanceAfter);

        // Then
        assertNotNull(operation);
        assertNotNull(operation.operationId());
        assertEquals(TEST_ACCOUNT_ID, operation.accountId());
        assertEquals(OperationType.DEPOSIT, operation.type());
        assertEquals(amount, operation.amount());
        assertEquals(balanceAfter, operation.balanceAfter());
        assertNotNull(operation.operationDate());
    }

    @Test
    void create_shouldCreateWithdrawOperation() {
        // Given
        BigDecimal amount = new BigDecimal("50.00");
        BigDecimal balanceAfter = new BigDecimal("950.00");

        // When
        Operation operation = Operation.create(TEST_ACCOUNT_ID, OperationType.WITHDRAW, amount, balanceAfter);

        // Then
        assertNotNull(operation);
        assertEquals(OperationType.WITHDRAW, operation.type());
        assertEquals(amount, operation.amount());
        assertEquals(balanceAfter, operation.balanceAfter());
    }

    // ========================================
    // VALIDATION TESTS
    // ========================================

    @ParameterizedTest(name = "Should throw NullPointerException when {6}")
    @MethodSource("nullFieldValidationProvider")
    void constructor_shouldThrowException_whenFieldIsNull(
            UUID operationId,
            UUID accountId,
            OperationType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            OffsetDateTime operationDate,
            String testCase
    ) {
        // When & Then
        assertThrows(NullPointerException.class, () ->
                new Operation(
                        operationId,
                        accountId,
                        type,
                        amount,
                        balanceAfter,
                        operationDate
                )
        );
    }

    @ParameterizedTest(name = "Should throw IllegalArgumentException when {1}")
    @MethodSource("invalidAmountProvider")
    void constructor_shouldThrowException_whenAmountIsInvalid(BigDecimal amount, String testCase) {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                new Operation(
                        TEST_OPERATION_ID,
                        TEST_ACCOUNT_ID,
                        OperationType.DEPOSIT,
                        amount,
                        TEST_BALANCE_AFTER,
                        TEST_DATE
                )
        );
    }

    // ========================================
    // EQUALS & HASHCODE TESTS
    // ========================================

    @Test
    void equals_shouldReturnTrue_whenOperationsAreEqual() {
        // Given
        OffsetDateTime now = OffsetDateTime.now();
        Operation operation1 = new Operation(
                TEST_OPERATION_ID,
                TEST_ACCOUNT_ID,
                OperationType.DEPOSIT,
                new BigDecimal("100"),
                new BigDecimal("1100"),
                now
        );
        Operation operation2 = new Operation(
                TEST_OPERATION_ID,
                TEST_ACCOUNT_ID,
                OperationType.DEPOSIT,
                new BigDecimal("100"),
                new BigDecimal("1100"),
                now
        );

        // When & Then
        assertEquals(operation1, operation2);
        assertEquals(operation1.hashCode(), operation2.hashCode());
    }

    @Test
    void toString_shouldContainAllFields() {
        // Given
        Operation operation = Operation.create(
                TEST_ACCOUNT_ID,
                OperationType.DEPOSIT,
                new BigDecimal("100"),
                new BigDecimal("1100")
        );

        // When
        String toString = operation.toString();

        // Then
        assertTrue(toString.contains("DEPOSIT"));
        assertTrue(toString.contains("100"));
        assertTrue(toString.contains("1100"));
    }
}
