package com.xuxiaoye.api.utils;

import io.micrometer.common.util.StringUtils;

import java.io.Serializable;

/**
 * Utility class for {@link Serializable} type operations.
 */
public class SerializableUtils {

    private SerializableUtils() {
    }

    /**
     * Checks if the given Serializable ID is "blank" (null or zero/empty value).
     *
     * @param id the Serializable ID to check
     * @return true if the ID is null, an empty string, or a numeric zero
     */
    public static boolean isSerializableBlank(Serializable id) {
        if (id == null) {
            return true;
        }
        if (id instanceof String s) {
            return StringUtils.isBlank(s);
        }
        if (id instanceof Number n) {
            return n.longValue() == 0L;
        }
        return false;
    }
}