package com.xuxiaoye.api.utils;

import java.io.IOException;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class FileUtils {
    private FileUtils() {
    }

    public static byte[] readFileToBytes(String path) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(path);
        return resource.getContentAsByteArray();
    }
}
