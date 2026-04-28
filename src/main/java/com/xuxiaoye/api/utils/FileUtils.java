package com.xuxiaoye.api.utils;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class FileUtils {
    private FileUtils() {
    }

    public static byte[] readFileToBytes(String path) throws IOException {
        try {
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            Resource resource = resourceLoader.getResource(path);
            return resource.getContentAsByteArray();
        } catch (IOException e) {
            FileInputStream fis = new FileInputStream(path);
            return IOUtils.toByteArray(fis);
        }
    }
}
