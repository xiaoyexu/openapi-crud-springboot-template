package com.xuxiaoye.api.utils;

import java.security.SecureRandom;

public class RandomUtils {
    private RandomUtils() {
    }

    private static final SecureRandom r = new SecureRandom();

    private static final StringBuilder buffer = new StringBuilder("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");

    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder();
        int range = buffer.length();
        for (int i = 0; i < length; i++) {
            sb.append(buffer.charAt(r.nextInt(range)));
        }
        return sb.toString();
    }
}
