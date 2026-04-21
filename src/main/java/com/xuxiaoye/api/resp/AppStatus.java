package com.xuxiaoye.api.resp;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@SuppressWarnings("java:S1068")
public class AppStatus implements Serializable {
    private String code;
    private String customizedCode;
    private String message;

    public boolean isOk() {
        return this.code.equals("200");
    }

    public boolean isBadRequest() {
        return this.code.equals("400");
    }

    public boolean isUnauthorized() {
        return this.code.equals("401");
    }

    public boolean isForbidden() {
        return this.code.equals("403");
    }

    public boolean isNotFound() {
        return this.code.equals("404");
    }

    public boolean isInternalError() {
        return this.code.equals("500");
    }

    public static AppStatus ok() {
        return AppStatus.builder().code("200").message("ok").build();
    }

    public static AppStatus badRequest() {
        return AppStatus.builder().code("400").message("Bad request").build();
    }

    public static AppStatus badRequest(String message) {
        return AppStatus.builder().code("400").message(message).build();
    }

    public static AppStatus badRequest(String customizedCode, String message) {
        return AppStatus.builder().code("400").customizedCode(customizedCode).message(message).build();
    }

    public static AppStatus unauthorized() {
        return AppStatus.builder().code("401").message("Unauthorized").build();
    }

    public static AppStatus unauthorized(String message) {
        return AppStatus.builder().code("401").message(message).build();
    }

    public static AppStatus unauthorized(String customizedCode, String message) {
        return AppStatus.builder().code("401").customizedCode(customizedCode).message(message).build();
    }

    public static AppStatus forbidden() {
        return AppStatus.builder().code("403").message("Insufficient permissions").build();
    }

    public static AppStatus forbidden(String message) {
        return AppStatus.builder().code("403").message(message).build();
    }

    public static AppStatus forbidden(String customizedCode, String message) {
        return AppStatus.builder().code("403").customizedCode(customizedCode).message(message).build();
    }

    public static AppStatus notFound() {
        return AppStatus.builder().code("404").message("Record not found").build();
    }

    public static AppStatus notFound(String message) {
        return AppStatus.builder().code("404").message(message).build();
    }

    public static AppStatus notFound(String customizedCode, String message) {
        return AppStatus.builder().code("404").customizedCode(customizedCode).message(message).build();
    }

    public static AppStatus internalError() {
        return AppStatus.builder().code("500").message("Internal Server Error").build();
    }

    public static AppStatus internalError(String message) {
        return AppStatus.builder().code("500").message(message).build();
    }

    public static AppStatus internalError(String customizedCode, String message) {
        return AppStatus.builder().code("500").customizedCode(customizedCode).message(message).build();
    }

    public static AppStatus fromHttpStatusCode(int code) {
        return AppStatus.builder().code("" + code).message("HTTP CODE " + code).build();
    }

    public static AppStatus fromHttpStatusCode(int code, String message) {
        return AppStatus.builder().code("" + code).message(message).build();
    }
}
