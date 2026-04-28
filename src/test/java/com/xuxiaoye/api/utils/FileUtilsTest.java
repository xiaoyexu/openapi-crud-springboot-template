package com.xuxiaoye.api.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {
    @Test
    void testLoadResourceFile() {
        assertDoesNotThrow(() -> {
            byte[] bytes = FileUtils.readFileToBytes("test_certs/test_pub_key.der");
            assertNotNull(bytes);
            assertTrue(bytes.length > 0);
        });
    }

    @Test
    void testLoadSystemFile() throws URISyntaxException {
        URL res = getClass().getClassLoader().getResource("test_certs/test_pub_key.der");
        File file = Paths.get(res.toURI()).toFile();
        assertDoesNotThrow(() -> {
            byte[] bytes = FileUtils.readFileToBytes(file.getAbsolutePath());
            assertNotNull(bytes);
            assertTrue(bytes.length > 0);
        });
    }

    @Test
    void testLoadFileNotExist() {
        assertThrows(IOException.class, () -> {
            FileUtils.readFileToBytes("no_such_file.txt");
        });
    }
}