package com.xuxiaoye.api.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RandomUtilsTest {
    @Test
    void testRandomString() {
        assertThat(RandomUtils.randomString(5)).hasSize(5);
    }
}