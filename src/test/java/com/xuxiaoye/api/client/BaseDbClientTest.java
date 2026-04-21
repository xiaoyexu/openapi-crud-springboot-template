package com.xuxiaoye.api.client;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.MyBatisSystemException;

import com.xuxiaoye.api.resp.AppResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BaseDbClientTest {
    EasyRandom easyRandom = new EasyRandom();
    BaseDbClient baseDbClient;

    @BeforeEach
    public void setup() {
        baseDbClient = new BaseDbClient();
    }

    @Test
    void testHandleDbCall() {
        String data = easyRandom.nextObject(String.class);

        AppResponse<Object> appResponse = baseDbClient.handleDbCall((() -> AppResponse.okWithData(data)));
        assertThat(appResponse).isNotNull();
        assertThat(appResponse.getData()).isEqualTo(data);
        assertThat(appResponse.getStatus().getCode()).isEqualTo("200");
        assertThat(appResponse.getStatus().getMessage()).isEqualTo("ok");
    }

    @Test
    void testHandleDbCallWithRuntimeException() {
        String errorMsg = easyRandom.nextObject(String.class);

        AppResponse<Object> appResponse = baseDbClient.handleDbCall(() -> {
            throw new RuntimeException(errorMsg);
        });
        assertThat(appResponse).isNotNull();
        assertThat(appResponse.getStatus().getCode()).isEqualTo("500");
        assertThat(appResponse.getStatus().getMessage()).isEqualTo(errorMsg);
    }

    @Test
    void testHandleDbCallWithMyBatisSystemException() {
        String errorMsg = easyRandom.nextObject(String.class);

        MyBatisSystemException exception = mock(MyBatisSystemException.class);
        when(exception.getLocalizedMessage()).thenReturn(errorMsg);
        AppResponse<Object> appResponse = baseDbClient.handleDbCall(() -> {
            throw exception;
        });
        assertThat(appResponse).isNotNull();
        assertThat(appResponse.getStatus().getCode()).isEqualTo("500");
        assertThat(appResponse.getStatus().getMessage()).isEqualTo(errorMsg);
    }
}