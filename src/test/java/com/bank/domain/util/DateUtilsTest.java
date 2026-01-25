package com.bank.domain.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for DateUtils.
 * Tests all utility methods for date operations.
 */
class DateUtilsTest {

    // ========================================
    // TEST: CONSTRUCTOR (Utility class should not be instantiable)
    // ========================================

    @Test
    void test_constructor_should_throw_exception() throws NoSuchMethodException {
        // Given
        Constructor<DateUtils> constructor = DateUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // When / Then
        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class)
                .cause()
                .hasMessage("Utility class cannot be instantiated");
    }

    // ========================================
    // TEST: isDateTodayOrFuture
    // ========================================

    @Test
    void test_isDateTodayOrFuture_should_return_true_when_date_is_today() {
        // Given
        LocalDate today = LocalDate.now();

        // When
        boolean result = DateUtils.isDateTodayOrFuture(today);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void test_isDateTodayOrFuture_should_return_true_when_date_is_in_future() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(1);

        // When
        boolean result = DateUtils.isDateTodayOrFuture(futureDate);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void test_isDateTodayOrFuture_should_return_false_when_date_is_in_past() {
        // Given
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // When
        boolean result = DateUtils.isDateTodayOrFuture(pastDate);

        // Then
        assertThat(result).isFalse();
    }

    // ========================================
    // TEST: isDateInPast
    // ========================================

    @Test
    void test_isDateInPast_should_return_true_when_date_is_in_past() {
        // Given
        LocalDate pastDate = LocalDate.now().minusDays(1);

        // When
        boolean result = DateUtils.isDateInPast(pastDate);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void test_isDateInPast_should_return_false_when_date_is_today() {
        // Given
        LocalDate today = LocalDate.now();

        // When
        boolean result = DateUtils.isDateInPast(today);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void test_isDateInPast_should_return_false_when_date_is_in_future() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(1);

        // When
        boolean result = DateUtils.isDateInPast(futureDate);

        // Then
        assertThat(result).isFalse();
    }

    // ========================================
    // TEST: isStartDateAfterEndDate
    // ========================================

    @Test
    void test_isStartDateAfterEndDate_should_return_true_when_start_after_end() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 31);
        LocalDate endDate = LocalDate.of(2026, 1, 1);

        // When
        boolean result = DateUtils.isStartDateAfterEndDate(startDate, endDate);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void test_isStartDateAfterEndDate_should_return_false_when_start_before_end() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        // When
        boolean result = DateUtils.isStartDateAfterEndDate(startDate, endDate);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void test_isStartDateAfterEndDate_should_return_false_when_dates_are_equal() {
        // Given
        LocalDate date = LocalDate.of(2026, 1, 15);

        // When
        boolean result = DateUtils.isStartDateAfterEndDate(date, date);

        // Then
        assertThat(result).isFalse();
    }
}
