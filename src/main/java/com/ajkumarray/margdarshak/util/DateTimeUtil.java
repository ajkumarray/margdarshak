package com.ajkumarray.margdarshak.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations.
 */
public final class DateTimeUtil {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private DateTimeUtil() {
        // Prevent instantiation
    }

    /**
     * Formats a LocalDateTime to a string in ISO format.
     *
     * @param dateTime The LocalDateTime to format
     * @return Formatted date-time string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    /**
     * Parses a date-time string to LocalDateTime.
     *
     * @param dateTimeStr The date-time string to parse
     * @return Parsed LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER) : null;
    }

    /**
     * Checks if a date-time is in the future.
     *
     * @param dateTime The date-time to check
     * @return true if the date-time is in the future, false otherwise
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Checks if a date-time is in the past.
     *
     * @param dateTime The date-time to check
     * @return true if the date-time is in the past, false otherwise
     */
    public static boolean isPast(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Adds days to the current date-time.
     *
     * @param days The number of days to add
     * @return The resulting date-time
     */
    public static LocalDateTime addDaysToNow(long days) {
        return LocalDateTime.now().plusDays(days);
    }

    /**
     * Calculates the number of days between two date-times.
     *
     * @param start The start date-time
     * @param end The end date-time
     * @return The number of days between the two date-times
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return java.time.Duration.between(start, end).toDays();
    }
} 