package com.bank.domain.util;

import java.time.LocalDate;

/**
 * Utility class for date operations.
 * Provides helper methods to check date conditions.
 */
public final class DateUtils {

    private DateUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks if the date is today or in the future.
     *
     * @param date the date to check
     * @return true if the date is today or in the future
     */
    public static boolean isDateTodayOrFuture(LocalDate date) {
        return !date.isBefore(LocalDate.now());
    }

    /**
     * Checks if the date is in the past.
     *
     * @param date the date to check
     * @return true if the date is in the past
     */
    public static boolean isDateInPast(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }

    /**
     * Checks if the start date is after the end date.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return true if start date is after end date
     */
    public static boolean isStartDateAfterEndDate(LocalDate startDate, LocalDate endDate) {
        return startDate.isAfter(endDate);
    }
}
