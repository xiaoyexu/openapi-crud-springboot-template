package com.xuxiaoye.api.utils;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.util.HtmlUtils;

import com.xuxiaoye.api.constant.LogConstant;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

public class LogUtils {
    private LogUtils() {
    }

    public static void updateBizKey(Map<String, Object> bizKey) throws IOException {
        String bizKeyStr = MDC.get(LogConstant.MDC_BIZ_KEYS);
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isNotBlank(bizKeyStr)) {
            map = JacksonUtils.toMap(bizKeyStr);
        }

        if (bizKey != null && !bizKey.isEmpty())
            map.putAll(bizKey);

        bizKeyStr = JacksonUtils.toString(map);

        if (!map.isEmpty()) {
            MDC.put(LogConstant.MDC_BIZ_KEYS, bizKeyStr);
        }
    }

    public static String maskWithEscape(String field) {
        return escapeLogMsg(mask(field));
    }

    public static String escapeLogMsg(String msg) {
        if (StringUtils.isNotBlank(msg)) {
            return escapeJava(HtmlUtils.htmlEscape(msg));
        }
        return "";
    }

    public static String mask(String s) {
        if (StringUtils.isBlank(s)) {
            return s;
        }
        // Special data mask logic for LocalDate (yyyy-mm-dd)
        return s.replaceAll(".(?=.{5})", "*");
    }

    public static String maskWithEscape(LocalDate field) {
        return escapeLogMsg(mask(field));
    }

    public static String mask(LocalDate date) {
        if (date == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedString = date.format(formatter);
        return formattedString.replaceAll("^\\d{4}", "****");
    }

    public static void initSecurityAuditBizKey() throws IOException {
        initCallerContextId();
        initContextId();
        initTraceId();
        Map<String, Object> bizKey = new HashMap<>();
        bizKey.put(LogConstant.MDC_BIZ_KEY_IP_ADDRESS, LogConstant.LOCALHOST_IP);
        bizKey.put(LogConstant.MDC_BIZ_KEY_USER_ID, LogConstant.SYSTEM_USER_ID);
        updateBizKey(bizKey);
    }

    private static void initContextId() {
        if (StringUtils.isBlank(MDC.get(LogConstant.CONTEXT_ID))) {
            MDC.put(LogConstant.CONTEXT_ID, LogUtils.generateContextId());
        }
    }

    private static void initCallerContextId() {
        if (StringUtils.isBlank(MDC.get(LogConstant.CALLER_CONTEXT_ID))) {
            MDC.put(LogConstant.CALLER_CONTEXT_ID, LogUtils.generateContextId());
        }
    }

    private static void initTraceId() {
        if (StringUtils.isBlank(MDC.get(LogConstant.TRACE_ID))) {
            MDC.put(LogConstant.TRACE_ID, LogUtils.generateTraceId());
        }
    }

    private static SecureRandom r = new SecureRandom();

    private static StringBuilder buffer = new StringBuilder("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");

    public static String generateContextId() {
        StringBuilder sb = new StringBuilder();
        int range = buffer.length();
        for (int i = 0; i < 5; i++) {
            sb.append(buffer.charAt(r.nextInt(range)));
        }
        return sb.toString();
    }

    public static String generateTraceId() {
        StringBuilder sb = new StringBuilder();
        int range = buffer.length();
        for (int i = 0; i < 10; i++) {
            sb.append(buffer.charAt(r.nextInt(range)));
        }
        return sb.toString();
    }
}
