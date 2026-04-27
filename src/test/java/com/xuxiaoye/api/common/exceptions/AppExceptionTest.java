package com.xuxiaoye.api.common.exceptions;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AppExceptionTest {
    EasyRandom easyRandom = new EasyRandom();

    @Test
    void testExceptions() {
        AppException appException = new AppException();
        assertThat(appException.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        String errorMsg = easyRandom.nextObject(String.class);
        appException = new AppException(errorMsg);
        assertThat(appException.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(appException.getMessage()).isEqualTo(errorMsg);

        BadRequestException badRequestException = new BadRequestException("");
        assertThat(badRequestException.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);

        UnauthorizedException unauthorizedException = new UnauthorizedException("");
        assertThat(unauthorizedException.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ForbiddenException forbiddenException = new ForbiddenException("");
        assertThat(forbiddenException.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);

        NotFoundException notFoundException = new NotFoundException("");
        assertThat(notFoundException.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        InternalServerErrorException internalServerErrorException = new InternalServerErrorException("");
        assertThat(internalServerErrorException.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}