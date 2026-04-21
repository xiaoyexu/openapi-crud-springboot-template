package com.xuxiaoye.api.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final String DATETIME_FORMATTER = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_FORMATTER_T = "yyyy-MM-ddTHH:mm:ss";
    public static final String DATE_FORMATTER = "yyyy-MM-dd";
    public static final String TIME_FORMATTER = "HH:mm:ss";

    private DateTimeUtils() {
    }

    // LocalDate
    public static LocalDate parseStringToDate(String dateStr, String format) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
    }

    public static String parseDateToString(LocalDate localDate, String targetPattern) {
        return localDate.format(DateTimeFormatter.ofPattern(targetPattern));
    }

    public static String reformatStringDate(String dateStr, String originalPattern, String targetPattern) {
        LocalDate formattedDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(originalPattern));
        return parseDateToString(formattedDate, targetPattern);
    }

    // LocalDateTime
    public static LocalDateTime parseStringToDateTime(String dateStr, String format) {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(format));
    }

    public static LocalDateTime parseStringToDateTime(String dateStr) {
        return parseStringToDateTime(dateStr, DATETIME_FORMATTER);
    }

    public static String parseDateTimeToString(LocalDateTime localDateTime, String targetPattern) {
        return localDateTime.format(DateTimeFormatter.ofPattern(targetPattern));
    }

    public static String reformatStringDateTime(String dateStr, String originalPattern, String targetPattern) {
        LocalDateTime formattedDate = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(originalPattern));
        return parseDateTimeToString(formattedDate, targetPattern);
    }
}
