package com.xuxiaoye.api.resp;

import java.io.Serializable;

import io.micrometer.common.util.StringUtils;
import lombok.Builder;
import lombok.Data;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class AppResponseTest {
    EasyRandom easyRandom = new EasyRandom();

    @Data
    @Builder
    static class TestResponse implements Serializable {

        private String data;
    }

    private void assertAppResponse(AppResponse<Object> response, Object data, String code, String msg) {
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getStatus()).isNotNull();
        assertThat(response.getStatus().getCode()).isEqualTo(code);
        assertThat(response.getStatus().getMessage()).isEqualTo(msg);
    }

    @Test
    void testConstructor() {
        String data = easyRandom.nextObject(String.class);
        String code = easyRandom.nextObject(String.class);
        String msg = easyRandom.nextObject(String.class);
        AppStatus appStatus = AppStatus.builder().code(code).message(msg).build();

        // Builder
        AppResponse<Object> response = AppResponse.builder().data(data).status(appStatus).build();
        assertAppResponse(response, data, code, msg);

        data = easyRandom.nextObject(String.class);
        code = easyRandom.nextObject(String.class);
        msg = easyRandom.nextObject(String.class);
        appStatus = new AppStatus(code, null, msg);
        response = new AppResponse<>(data, appStatus);

        assertAppResponse(response, data, code, msg);
    }

    @Test
    void testEqual() {
        EqualsVerifier.simple().forClass(AppResponse.class).verify();
    }

    @Test
    void testBuildSuccessAppResponse() {
        String data = easyRandom.nextObject(String.class);
        AppResponse<String> response = AppResponse.okWithData(data);

        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getStatus()).isNotNull();
        assertThat(response.getStatus().getCode()).isEqualTo("200");
        assertThat(response.getStatus().getMessage()).isEqualTo("ok");
        assertThat(response.isOk()).isTrue();
    }

    @Test
    void testBuildNullSuccessAppResponse() {
        AppResponse<String> response = AppResponse.ok();

        assertThat(response.getData()).isEqualTo(null);
        assertThat(response.getStatus()).isNotNull();
        assertThat(response.getStatus().getCode()).isEqualTo("200");
        assertThat(response.getStatus().getMessage()).isEqualTo("ok");
        assertThat(response.isOk()).isTrue();
    }

    @Test
    void testBuildErrorAppResponse() {
        String code = easyRandom.nextObject(String.class);
        String msg = easyRandom.nextObject(String.class);
        AppStatus appStatus = AppStatus.builder().code(code).message(msg).build();

        AppResponse<String> response = AppResponse.failWithStatus(appStatus);
        assertThat(response.getData()).isNull();
        assertThat(response.getStatus()).isNotNull();
        assertThat(response.getStatus().getCode()).isEqualTo(code);
        assertThat(response.getStatus().getMessage()).isEqualTo(msg);
        assertThat(response.isOk()).isFalse();
    }

    @Test
    void testToResponseEntityOk() {
        String data = easyRandom.nextObject(String.class);
        AppResponse<String> response = AppResponse.okWithData(data);

        ResponseEntity<Serializable> responseEntity = response.toResponseEntity(
                d -> TestResponse.builder().data(d).build()
                ,
                status -> status
        );
        TestResponse testResponse = (TestResponse) responseEntity.getBody();
        assertThat(testResponse).isNotNull();
        assertThat(testResponse.data).isEqualTo(data);
    }

    @Test
    void testAppResponse() {
        AppResponse<Object> appResponse = AppResponse.failWithStatus(AppStatus.badRequest());
        assertThat(appResponse.isBadRequest()).isTrue();

        appResponse = AppResponse.failWithStatus(AppStatus.unauthorized());
        assertThat(appResponse.isUnauthorized()).isTrue();

        appResponse = AppResponse.failWithStatus(AppStatus.forbidden());
        assertThat(appResponse.isForbidden()).isTrue();

        appResponse = AppResponse.failWithStatus(AppStatus.notFound());
        assertThat(appResponse.isNotFound()).isTrue();

        appResponse = AppResponse.failWithStatus(AppStatus.internalError());
        assertThat(appResponse.isInternalError()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "400,,BAD_REQUEST",
            "401,,UNAUTHORIZED",
            "403,,FORBIDDEN",
            "404,,NOT_FOUND",
            "503,,SERVICE_UNAVAILABLE",
            "500,,INTERNAL_SERVER_ERROR",
            //
            "400,001,BAD_REQUEST",
            "401,002,UNAUTHORIZED",
            "403,003,FORBIDDEN",
            "404,004,NOT_FOUND",
            "503,005,SERVICE_UNAVAILABLE",
            "500,006,INTERNAL_SERVER_ERROR",
    })
    void testToResponseEntityWithError(String code, String customizedCode, String httpStatusValue) {
        String msg = easyRandom.nextObject(String.class);

        if (StringUtils.isNotEmpty(customizedCode)) {
            AppStatus appStatus = AppStatus.builder().code(code).customizedCode(customizedCode).message(msg).build();
            AppResponse<String> response = AppResponse.failWithStatus(appStatus);

            ResponseEntity<Serializable> responseEntity = response.toResponseEntity(
                    data -> TestResponse.builder().data(data).build()
                    ,
                    status -> status
            );
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(httpStatusValue));
        } else {
            AppStatus appStatus = AppStatus.builder().code(code).message(msg).build();
            AppResponse<String> response = AppResponse.failWithStatus(appStatus);

            ResponseEntity<Serializable> responseEntity = response.toResponseEntity(
                    data -> TestResponse.builder().data(data).build()
                    ,
                    status -> status
            );
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(httpStatusValue));
        }
    }

    @Test
    void testIfOkElseOk() {
        String value = easyRandom.nextObject(String.class);
        AppResponse<String> response = AppResponse.okWithData(value);

        AppResponse<Integer> parsedResponse = response.ifOkElse(
                String::length,
                status -> status
        );
        assertThat(parsedResponse.isOk()).isTrue();
        assertThat(parsedResponse.getData()).isEqualTo(value.length());
    }

    @Test
    void testIfOkElseError() {
        String code = easyRandom.nextObject(String.class);
        String msg = easyRandom.nextObject(String.class);
        AppStatus appStatus = AppStatus.builder().code(code).message(msg).build();

        String newCode = easyRandom.nextObject(String.class);
        String newMsg = easyRandom.nextObject(String.class);
        AppStatus newAppStatus = AppStatus.builder().code(newCode).message(newMsg).build();

        AppResponse<String> response = AppResponse.failWithStatus(appStatus);

        AppResponse<Integer> parsedResponse = response.ifOk(
                String::length
        );
        assertThat(parsedResponse.isOk()).isFalse();
        assertThat(parsedResponse.getStatus().getCode()).isEqualTo(code);
        assertThat(parsedResponse.getStatus().getMessage()).isEqualTo(msg);

        parsedResponse = response.ifOkElse(
                String::length,
                status -> newAppStatus
        );
        assertThat(parsedResponse.isOk()).isFalse();
        assertThat(parsedResponse.getStatus().getCode()).isEqualTo(newCode);
        assertThat(parsedResponse.getStatus().getMessage()).isEqualTo(newMsg);
    }

    @Test
    void testIfOkElseErrorStatus() {
        String value = easyRandom.nextObject(String.class);
        String code = easyRandom.nextObject(String.class);
        String msg = easyRandom.nextObject(String.class);
        AppStatus appStatus = AppStatus.builder().code(code).message(msg).build();
        AppResponse<String> response = AppResponse.failWithDataStatus(value, appStatus);

        AppResponse<Integer> parsedResponse = response.ifOkElse(
                String::length,
                (data, status) -> AppResponse.okWithData(data.length())
        );
        assertThat(parsedResponse.isOk()).isTrue();
        assertThat(parsedResponse.getData()).isEqualTo(value.length());
        assertThat(parsedResponse.getStatus().getCode()).isEqualTo("200");
        assertThat(parsedResponse.getStatus().getMessage()).isEqualTo("ok");

        AppResponse<String> okResponse = AppResponse.okWithData(value);
        AppResponse<Integer> okParsedResponse = okResponse.ifOkElse(
                String::length,
                (data, status) -> AppResponse.okWithData(data.length())
        );
        assertThat(okParsedResponse.isOk()).isTrue();
        assertThat(okParsedResponse.getData()).isEqualTo(value.length());
        assertThat(okParsedResponse.getStatus().getCode()).isEqualTo("200");
        assertThat(okParsedResponse.getStatus().getMessage()).isEqualTo("ok");
    }
}