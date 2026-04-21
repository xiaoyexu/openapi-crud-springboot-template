package com.xuxiaoye.api.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class DateTimeUtilsTest {
    @Nested
    class LocalDateTest {

        @ParameterizedTest
        @CsvSource({
                "2026-01-22,yyyy-MM-dd,2026,1,22",
                "2026-01-22 09:33:20,yyyy-MM-dd HH:mm:ss,2026,1,22",
                "2026-12-31 23:59:59,yyyy-MM-dd HH:mm:ss,2026,12,31",
                "2026-12-31T23:59:59,yyyy-MM-dd'T'HH:mm:ss,2026,12,31",
                "2026-12-31 23:59:59,yyyy-MM-dd HH:mm:ss[.][SSS][SS][S],2026,12,31",
                "2026-12-31 23:59:59.1,yyyy-MM-dd HH:mm:ss[.][SSS][SS][S],2026,12,31",
                "2026-12-31 23:59:59.12,yyyy-MM-dd HH:mm:ss[.][SSS][SS][S],2026,12,31",
                "2026-12-31 23:59:59.123,yyyy-MM-dd HH:mm:ss[.][SSS][SS][S],2026,12,31",
        })
        void testLocalDateParsing(String strDate, String format, int year, int month, int day) {
            LocalDate date = DateTimeUtils.parseStringToDate(strDate, format);
            assertThat(date.getYear()).isEqualTo(year);
            assertThat(date.getMonth()).isEqualTo(Month.of(month));
            assertThat(date.getDayOfMonth()).isEqualTo(day);
        }

        @ParameterizedTest
        @CsvSource({
                "2026,12,31,yyyy-MM-dd,2026-12-31",
                "2026,12,31,yyyy.MM.dd,2026.12.31",
                "2026,12,30,yyyy MM dd,2026 12 30",
                "2026,12,29,MM dd/yyyy,12 29/2026"
        })
        void testParseDate(int year, int month, int day, String format, String expect) {
            assertThat(DateTimeUtils.parseDateToString(LocalDate.of(year, month, day), format)).isEqualTo(expect);
        }

        @ParameterizedTest
        @CsvSource({
                "2026-12-31,yyyy-MM-dd,yyyy.MM.dd,2026.12.31",
                "2026.12.31,yyyy.MM.dd,dd/MM/yyyy,31/12/2026",
        })
        void testReformat(String strDate, String fromFormat, String toFormat, String expect) {
            assertThat(DateTimeUtils.reformatStringDate(strDate, fromFormat, toFormat)).isEqualTo(expect);
        }
    }

    @Nested
    class LocalDateTimeTest {

        @ParameterizedTest
        @CsvSource({
                "2026-01-22 09:33:20,yyyy-MM-dd HH:mm:ss,2026,1,22,9,33,20,0",
                "2026-12-31 23:59:59,yyyy-MM-dd HH:mm:ss,2026,12,31,23,59,59,0",
                "2026-12-31T23:59:59,yyyy-MM-dd'T'HH:mm:ss,2026,12,31,23,59,59,0",
                "2026-12-31 23:59:59,yyyy-MM-dd HH:mm:ss[.][SSS][SS][S],2026,12,31,23,59,59,0",
                "2026-12-31 23:59:59.1,yyyy-MM-dd HH:mm:ss[.][SSS][SS][S],2026,12,31,23,59,59,100000000",
                "2026-12-31 23:59:59.12,yyyy-MM-dd HH:mm:ss[.][SSS][SS][S],2026,12,31,23,59,59,120000000",
                "2026-12-31 23:59:59.123,yyyy-MM-dd HH:mm:ss[.][SSS][SS][S],2026,12,31,23,59,59,123000000",
        })
        void testLocalDateTimeParsing(String strDate, String format, int year, int month, int day, int hour, int minute, int second, int nano) {
            LocalDateTime dateTime = DateTimeUtils.parseStringToDateTime(strDate, format);
            assertThat(dateTime.getYear()).isEqualTo(year);
            assertThat(dateTime.getMonth()).isEqualTo(Month.of(month));
            assertThat(dateTime.getDayOfMonth()).isEqualTo(day);
            assertThat(dateTime.getHour()).isEqualTo(hour);
            assertThat(dateTime.getMinute()).isEqualTo(minute);
            assertThat(dateTime.getSecond()).isEqualTo(second);
            assertThat(dateTime.getNano()).isEqualTo(nano);
        }

        @ParameterizedTest
        @CsvSource({
                "2026,12,31,10,28,31,100000000,yyyy-MM-dd HH:mm:ss,2026-12-31 10:28:31",
                "2026,12,31,10,28,31,100000000,yyyy-MM-dd'T'HH:mm:ss,2026-12-31T10:28:31",

                // 100000000
                "2026,12,31,10,28,31,100000000,yyyy-MM-dd HH:mm:ss.S,2026-12-31 10:28:31.1",
                "2026,12,31,10,28,31,100000000,yyyy-MM-dd HH:mm:ss.SS,2026-12-31 10:28:31.10",
                "2026,12,31,10,28,31,100000000,yyyy-MM-dd HH:mm:ss.SSS,2026-12-31 10:28:31.100",
                // 123000000
                "2026,12,31,10,28,31,123000000,yyyy-MM-dd HH:mm:ss.S,2026-12-31 10:28:31.1",
                "2026,12,31,10,28,31,123000000,yyyy-MM-dd HH:mm:ss.SS,2026-12-31 10:28:31.12",
                "2026,12,31,10,28,31,123000000,yyyy-MM-dd HH:mm:ss.SSS,2026-12-31 10:28:31.123",
        })
        void testParseDateTime(int year, int month, int day, int hour, int minute, int second, int nano, String format, String expect) {
            assertThat(DateTimeUtils.parseDateTimeToString(LocalDateTime.of(
                    year, month, day,
                    hour, minute, second,
                    nano
            ), format)).isEqualTo(expect);
        }

        @ParameterizedTest
        @CsvSource({
                "2026-12-31T12:31:23,yyyy-MM-dd'T'HH:mm:ss,yyyy.MM.dd'T'HH:mm:ss,2026.12.31T12:31:23",
                "2026.12.31 12:59:34,yyyy.MM.dd HH:mm:ss,HH:mm:ss dd/MM/yyyy,12:59:34 31/12/2026",
        })
        void testReformat(String strDate, String fromFormat, String toFormat, String expect) {
            assertThat(DateTimeUtils.reformatStringDateTime(strDate, fromFormat, toFormat)).isEqualTo(expect);
        }
    }
}