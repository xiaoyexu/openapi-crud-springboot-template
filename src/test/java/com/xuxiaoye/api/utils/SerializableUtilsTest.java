package com.xuxiaoye.api.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SerializableUtilsTest {

    // --- Null handling ---

    @Test
    void should_return_true_when_id_is_null() {
        assertTrue(SerializableUtils.isSerializableBlank(null));
    }

    // --- String handling ---

    @Test
    void should_return_true_when_id_is_blank_string() {
        assertTrue(SerializableUtils.isSerializableBlank(""));
        assertTrue(SerializableUtils.isSerializableBlank("   "));
        assertTrue(SerializableUtils.isSerializableBlank("\t"));
    }

    @Test
    void should_return_false_when_id_is_non_blank_string() {
        assertFalse(SerializableUtils.isSerializableBlank("abc"));
        assertFalse(SerializableUtils.isSerializableBlank("123"));
    }

    // --- Number handling ---

    @Test
    void should_return_true_when_id_is_zero_number() {
        assertTrue(SerializableUtils.isSerializableBlank(0));
        assertTrue(SerializableUtils.isSerializableBlank(0L));
        assertTrue(SerializableUtils.isSerializableBlank(0.0));
        assertTrue(SerializableUtils.isSerializableBlank(BigDecimal.ZERO));
        assertTrue(SerializableUtils.isSerializableBlank((short) 0));
        assertTrue(SerializableUtils.isSerializableBlank((byte) 0));
        assertTrue(SerializableUtils.isSerializableBlank(Integer.valueOf(0)));
    }

    @Test
    void should_return_false_when_id_is_non_zero_number() {
        assertFalse(SerializableUtils.isSerializableBlank(1));
        assertFalse(SerializableUtils.isSerializableBlank(-1L));
        assertFalse(SerializableUtils.isSerializableBlank(1.5));
        assertFalse(SerializableUtils.isSerializableBlank(BigDecimal.valueOf(42)));
    }

    // --- Non-null, non-string, non-number ---

    @Test
    void should_return_false_when_id_is_other_serializable() {
        // Custom serializable object - not null, not String, not Number
        assertFalse(SerializableUtils.isSerializableBlank(new CustomSerializable()));
    }

    // A simple test helper
    private static class CustomSerializable implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
    }
}