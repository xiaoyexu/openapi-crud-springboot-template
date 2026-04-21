package com.xuxiaoye.api.utils;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import com.xuxiaoye.api.constant.LogConstant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

class LogUtilsTest {

    @Test
    void escapeLogMsgTest() {
        String result = LogUtils.escapeLogMsg("");
        assertThat(result).isEqualTo("");

        result = LogUtils.escapeLogMsg(null);
        assertThat(result).isEqualTo("");

        result = LogUtils.escapeLogMsg("abc");
        assertThat(result).isEqualTo("abc");

        result = LogUtils.escapeLogMsg("<h1>hello world</h1>");
        assertThat(result).isEqualTo("&lt;h1&gt;hello world&lt;/h1&gt;");
    }

    @Test
    void maskDataWithEscapeTest() {
        String result = LogUtils.maskWithEscape("abcdefg");
        assertThat(result).isEqualTo("**cdefg");

        result = LogUtils.maskWithEscape("<h1>abc</h1>");
        assertThat(result).isEqualTo("*******&lt;/h1&gt;");
    }

    @Test
    void maskDataTest() {
        String result = LogUtils.mask("");
        assertThat(result).isEqualTo("");

        String value = "abcdefg";
        result = LogUtils.mask(value);
        assertThat(result).isEqualTo("**cdefg");
    }


    @Test
    void generateContextIdTest() {
        String target = LogUtils.generateContextId();
        assertThat(target.length()).isEqualTo(5);
    }

    @Test
    void generateTraceIdTest() {
        String target = LogUtils.generateTraceId();
        assertThat(target.length()).isEqualTo(10);
    }

    @Test
    void maskWithEscape() {
        LocalDate date = LocalDate.of(2022, 1, 31);
        String result = LogUtils.maskWithEscape(date);
        assertThat(result).isEqualTo("****-01-31");
    }

    @Test
    void mask() {
        LocalDate date = LocalDate.of(2022, 1, 31);
        String result = LogUtils.mask(date);
        assertThat(result).isEqualTo("****-01-31");
    }

    @Test
    void initSecurityAuditBizKey() {
        try (var mockMDC = mockStatic(MDC.class)) {
            mockMDC.when(() -> MDC.get(LogConstant.MDC_BIZ_KEYS)).thenReturn("{\"name\":\"value\"}");
            assertDoesNotThrow(LogUtils::initSecurityAuditBizKey);
        }
    }

    @Test
    void updateBizKey() {
        assertDoesNotThrow(() -> LogUtils.updateBizKey(null));
        assertDoesNotThrow(() -> LogUtils.updateBizKey(Map.of()));
    }
}