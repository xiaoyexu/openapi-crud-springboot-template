package com.xuxiaoye.api.utils;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonUtilsTest {

    record JsonClass(String name) {
    }

    @Test
    void testToString() throws JsonProcessingException {
        JsonClass jsonClass = new JsonClass("test");
        String result = JacksonUtils.toString(jsonClass);
        assertThat(result).isEqualTo("{\"name\":\"test\"}");
    }

    @Test
    void testToItem() throws JsonProcessingException {
        String jsonString = "{\"name\":\"test\"}";
        JsonClass jsonClass = JacksonUtils.toItem(jsonString, JsonClass.class);
        assertThat(jsonClass.name).isEqualTo("test");
    }

    @Test
    void testToMap() throws IOException {
        String jsonString = "{\"name\":\"test\"}";
        Map result = JacksonUtils.toMap(jsonString);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get("name")).isEqualTo("test");
    }

    @Test
    void testToStringMap() throws IOException {
        String jsonString = "{\"name\":\"test\"}";
        Map<String, String> result = JacksonUtils.toStringMap(jsonString);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get("name")).isEqualTo("test");
    }

    @Test
    void testToStringMapWithNonStringValue() throws IOException {
        String jsonString = "{\"name\":123}";
        Map<String, String> result = JacksonUtils.toStringMap(jsonString);
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    void testToStringMapWithException() throws IOException {
        String jsonString = "{\"name\":123";
        Map<String, String> result = JacksonUtils.toStringMap(jsonString);
        assertThat(result).isEmpty();
    }

    @Test
    void testStringify() {
        JsonClass jsonClass = new JsonClass("test");
        String result = JacksonUtils.stringify(jsonClass);
        assertThat(result).isEqualTo("{\"name\":\"test\"}");
    }

    @Test
    void testParse() {
        String jsonString = "{\"name\":\"test\"}";
        JsonClass jsonClass = JacksonUtils.parse(jsonString, JsonClass.class);
        assertThat(jsonClass.name).isEqualTo("test");
    }

    @Test
    void testParseWithException() {
        String jsonString = "not a json string";
        JsonClass jsonClass = JacksonUtils.parse(jsonString, JsonClass.class);
        assertThat(jsonClass).isNull();
    }
}