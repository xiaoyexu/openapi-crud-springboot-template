package com.xuxiaoye.api.resp;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@Builder
@AllArgsConstructor
public class AppResponse<T> implements Serializable {
    @SuppressWarnings("java:S1948")
    private T data;
    private AppStatus status;

    public <R> ResponseEntity<R> toResponseEntity(Function<T, R> okFunc, Function<AppStatus, R> errorFunc) {
        if (status.isOk()) {
            R result = okFunc.apply(this.data);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        R result = errorFunc.apply(this.status);
        return switch (status.getCode()) {
            case "400" -> new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
            case "401" -> new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
            case "403" -> new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
            case "404" -> new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
            case "503" -> new ResponseEntity<>(result, HttpStatus.SERVICE_UNAVAILABLE);
            default -> new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }

    public static <D> AppResponse<D> okWithData(D data) {
        return new AppResponse<>(data, AppStatus.ok());
    }

    public static <D> AppResponse<D> ok() {
        return new AppResponse<>(null, AppStatus.ok());
    }

    public static <D> AppResponse<D> failWithStatus(AppStatus status) {
        return new AppResponse<>(null, status);
    }

    public static <D> AppResponse<D> failWithDataStatus(D data, AppStatus status) {
        return new AppResponse<>(data, status);
    }

    public boolean isOk() {
        return this.status.isOk();
    }

    public boolean isBadRequest() {
        return this.status.isBadRequest();
    }

    public boolean isUnauthorized() {
        return this.status.isUnauthorized();
    }

    public boolean isForbidden() {
        return this.status.isForbidden();
    }

    public boolean isNotFound() {
        return this.status.isNotFound();
    }

    public boolean isInternalError() {
        return this.status.isInternalError();
    }

    public <D> AppResponse<D> ifOk(Function<T, D> okFunc) {
        return ifOkElse(okFunc, (UnaryOperator<AppStatus>) null);
    }

    public <D> AppResponse<D> ifOkElse(Function<T, D> okFunc, UnaryOperator<AppStatus> errorFunc) {
        if (status.isOk()) {
            return AppResponse.okWithData(okFunc.apply(this.data));
        }
        if (errorFunc != null) {
            AppStatus result = errorFunc.apply(this.status);
            return AppResponse.failWithStatus(result);
        }
        return AppResponse.failWithStatus(this.status);
    }

    public <D> AppResponse<D> ifOkElse(Function<T, D> okFunc, BiFunction<T, AppStatus, AppResponse<D>> errorFunc) {
        if (status.isOk()) {
            return AppResponse.okWithData(okFunc.apply(this.data));
        }
        if (errorFunc != null) {
            return errorFunc.apply(this.data, this.status);
        }
        return AppResponse.failWithStatus(this.status);
    }
}
