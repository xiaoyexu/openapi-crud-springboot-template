package com.xuxiaoye.api.common.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.security.authorization.AuthorizationDeniedException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    @Test
    void testGlobalExceptionHandler() {
        assertDoesNotThrow(() -> {
            GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
            assertNotNull(globalExceptionHandler.handlerException(new Exception("abc"), null));
            assertNotNull(globalExceptionHandler.handlerAuthorizationDeniedException(new AuthorizationDeniedException("abc"), null));
        });
    }
}