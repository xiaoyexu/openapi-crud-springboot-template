package com.xuxiaoye.api.common.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@Data
@EqualsAndHashCode(callSuper = true)
public class JWTExpiredException extends AppException {
    public JWTExpiredException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
