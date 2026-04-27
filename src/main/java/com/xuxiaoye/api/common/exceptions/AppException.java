package com.xuxiaoye.api.common.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

import com.xuxiaoye.api.resp.ResponseStatus;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppException extends RuntimeException {
    protected final HttpStatus httpStatus;
    protected final ResponseStatus status;

    public AppException() {
        super();
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.status = ResponseStatus.builder().code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())).message("Application Internal Error").build();
    }

    public AppException(String message) {
        super(message);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.status = ResponseStatus.builder().code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())).message(message).build();
    }

    public AppException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.status = ResponseStatus.builder().code(String.valueOf(httpStatus.value())).message(message).build();
    }
}
