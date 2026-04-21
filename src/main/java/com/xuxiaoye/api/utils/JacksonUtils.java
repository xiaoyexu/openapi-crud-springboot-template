package com.xuxiaoye.api.utils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

public class JacksonUtils {
    private JacksonUtils() {
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(SerializationFeature.EAGER_SERIALIZER_FETCH);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.getFactory().disable(JsonFactory.Feature.INTERN_FIELD_NAMES);
        objectMapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);
        JavaTimeModule module = new JavaTimeModule();
        LocalDateDeserializer dateDeserializer = new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDateSerializer dateSerializer = new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        module.addDeserializer(LocalDate.class, dateDeserializer);
        module.addSerializer(LocalDate.class, dateSerializer);
        objectMapper.registerModule(module);
    }

    public static String toString(Object item) throws JsonProcessingException {
        return objectMapper.writeValueAsString(item);
    }

    public static <T> T toItem(String json, Class<T> type) throws JsonProcessingException {
        return objectMapper.readValue(json, type);
    }

    public static Map<String, Object> toMap(String json) throws IOException {
        return objectMapper.readValue(json, Map.class);
    }

    public static Map<String, String> toStringMap(String json) {
        try {
            Map<String, Object> mapObject = toMap(json);
            Map<String, String> mapString = new HashMap<>();
            for (Map.Entry<String, Object> entry : mapObject.entrySet()) {
                if (entry.getValue() instanceof String string) {
                    mapString.put(entry.getKey(), string);
                }
            }
            return mapString;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public static String stringify(Object item) {
        try {
            return toString(item);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T parse(String json, Class<T> type) {
        try {
            return toItem(json, type);
        } catch (Exception e) {
            return null;
        }
    }
}
