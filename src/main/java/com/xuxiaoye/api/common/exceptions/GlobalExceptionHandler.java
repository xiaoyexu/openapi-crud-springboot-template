package com.xuxiaoye.api.common.exceptions;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.xuxiaoye.api.resp.ErrorResponse;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ResponseEntity<ErrorResponse> handlerException(Exception ex, WebRequest webRequest) {
        log.error(ex.getLocalizedMessage(), ex);
        com.xuxiaoye.api.resp.ResponseStatus code = new com.xuxiaoye.api.resp.ResponseStatus();
        code.setCode("500");
        code.setMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        ErrorResponse response = new ErrorResponse(code);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    @ExceptionHandler({AppException.class})
//    public @ResponseBody ResponseEntity<ErrorResponse> handlerAppException(AppException ex, WebRequest webRequest) {
//        log.error(ex.getLocalizedMessage());
//        ErrorResponse response = new ErrorResponse(ex.getStatus());
//        return new ResponseEntity<>(response, ex.getHttpStatus());
//    }

    @ExceptionHandler({AuthorizationDeniedException.class})
    public @ResponseBody ResponseEntity<ErrorResponse> handlerAuthorizationDeniedException(AuthorizationDeniedException ex, WebRequest webRequest) {
        log.error(ex.getLocalizedMessage());
        ErrorResponse response = new ErrorResponse(new com.xuxiaoye.api.resp.ResponseStatus("401", "Unauthorized"));
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}