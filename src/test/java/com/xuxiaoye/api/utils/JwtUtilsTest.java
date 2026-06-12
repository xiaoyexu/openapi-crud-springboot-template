package com.xuxiaoye.api.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import io.jsonwebtoken.Claims;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {
    EasyRandom easyRandom = new EasyRandom();

    @Test
    void testHash() {
        assertThat(JwtUtils.getSHA256("username1")).isEqualTo("e1075933b26b5e4e50ab3dc3528eb3461214ba15b7a27b51f5dbc086912caf56");
        assertThat(JwtUtils.getSHA256("username2")).isEqualTo("bbc2711bf1d1c152b21b00cdadf89fa637c76238ffc871b17e1bb5ca9908a104");
        assertThat(JwtUtils.getSHA256("username3")).isEqualTo("d68379725d2d8a16e5f2cbcaf18e0baf84ea0aed81ed55b7c4f479e7cdab855d");
        assertThat(JwtUtils.getSHA256("username4")).isEqualTo("c9366c1bd693018b9ac3c43f1404d3063e7c62b73f93a3d3b17ec71411d5f10f");
        assertThat(JwtUtils.getSHA256("username5")).isEqualTo("253f4f19eb4886c3e9094b92c0080f5b4eecc52011669551a4cc96adb798d31c");
        assertThat(JwtUtils.getSHA256("username6")).isEqualTo("35632947cbe8b271f211797a975dfce06afcfc0b144adb0573f81df3f6bdb7f1");
    }

    @Test
    void testGenerateJWTToken() throws IOException {
        Map claims = Map.of(
                "Name", "ABC"
        );
        String token = JwtUtils.generateJWTToken(
                FileUtils.readFileToBytes("test_certs/test_pri_key.der"),
                easyRandom.nextObject(String.class),
                claims
        );
        assertThat(token).isNotNull();

        Claims extractedClaims = JwtUtils.validateJWTToken(token, FileUtils.readFileToBytes("test_certs/test_pub_key.der"));
        assertThat(extractedClaims).isNotNull();
        assertThat(extractedClaims.containsKey("Name")).isTrue();
        assertThat(extractedClaims.get("Name")).isEqualTo("ABC");
    }

    @Test
    void testGenerateJWTTokenError() {
        assertThrows(FileNotFoundException.class, () -> {
            Map claims = Map.of(
                    "Name", "ABC"
            );
            JwtUtils.generateJWTToken(
                    FileUtils.readFileToBytes("abc"),
                    easyRandom.nextObject(String.class),
                    claims
            );
        });

        assertThrows(RuntimeException.class, () -> {
            Map claims = Map.of(
                    "Name", "ABC"
            );
            JwtUtils.generateJWTToken(
                    FileUtils.readFileToBytes("test_certs/test.key"),
                    easyRandom.nextObject(String.class),
                    claims
            );
        });
    }

    @Test
    void testValidateJWTTokenError() throws IOException {
        Map claims = Map.of(
                "Name", "ABC"
        );
        String token = JwtUtils.generateJWTToken(
                FileUtils.readFileToBytes("test_certs/test_pri_key.der"),
                easyRandom.nextObject(String.class),
                claims
        );
        assertThat(token).isNotNull();

        assertThrows(RuntimeException.class, () -> {
            JwtUtils.validateJWTToken(token, FileUtils.readFileToBytes("test_certs/test_pub_key2.der"));
        });
    }
}