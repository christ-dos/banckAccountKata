package com.bank.domain.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DateUtils.
 * Tests all utility methods for date operations.
 */
class DateUtilsTest {

    @Test
    void test_constructor_should_throw_exception() throws NoSuchMethodException {
        Constructor<DateUtils> constructor = DateUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
        assertEquals("Utility class cannot be instantiated", exception.getCause().getMessage());
    }

    @Test
    void test_isDateTodayOrFuture_should_return_true_when_date_is_today() {
        LocalDate today = LocalDate.now();

        boolean result = DateUtils.isDateTodayOrFuture(today);

        assertTrue(result);
    }

    @Test
    void test_isDateTodayOrFuture_should_return_true_when_date_is_in_future() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        boolean result = DateUtils.isDateTodayOrFuture(futureDate);

        assertTrue(result);
    }

    @Test
    void test_isDateTodayOrFuture_should_return_false_when_date_is_in_past() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        boolean result = DateUtils.isDateTodayOrFuture(pastDate);

        assertFalse(result);
    }

    @Test
    void test_isDateInPast_should_return_true_when_date_is_in_past() {
        LocalDate pastDate = LocalDate.now().minusDays(1);

        boolean result = DateUtils.isDateInPast(pastDate);

        assertTrue(result);
    }

    @Test
    void test_isDateInPast_should_return_false_when_date_is_today() {
        LocalDate today = LocalDate.now();

        boolean result = DateUtils.isDateInPast(today);

        assertFalse(result);
    }

    @Test
    void test_isDateInPast_should_return_false_when_date_is_in_future() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        boolean result = DateUtils.isDateInPast(futureDate);

        assertFalse(result);
    }

    @Test
    void test_isStartDateAfterEndDate_should_return_true_when_start_after_end() {
        LocalDate startDate = LocalDate.of(2026, 1, 31);
        LocalDate endDate = LocalDate.of(2026, 1, 1);

        boolean result = DateUtils.isStartDateAfterEndDate(startDate, endDate);

        assertTrue(result);
    }

    @Test
    void test_isStartDateAfterEndDate_should_return_false_when_start_before_end() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        boolean result = DateUtils.isStartDateAfterEndDate(startDate, endDate);

        assertFalse(result);
    }

    @Test
    void test_isStartDateAfterEndDate_should_return_false_when_dates_are_equal() {
        LocalDate date = LocalDate.of(2026, 1, 15);

        boolean result = DateUtils.isStartDateAfterEndDate(date, date);

        assertFalse(result);
    }
}
