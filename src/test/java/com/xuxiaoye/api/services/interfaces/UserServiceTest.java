package com.xuxiaoye.api.services.interfaces;

import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.conf.ResourceConfig;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.services.UserServiceImpl;
import com.xuxiaoye.api.services.db.UserDBService;
import com.xuxiaoye.api.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.log4j.Log4j2;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.ActiveProfiles;

import com.xuxiaoye.api.Application;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
class UserServiceTest {
    EasyRandom easyRandom = new EasyRandom();

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    RequestContext requestContext;

    @Mock
    UserDBService userDBService;

    @Mock
    ResourceConfig resourceConfig;

    @Test
    void testWhenInvalidAccess() {

        when(resourceConfig.getPublicKey()).thenReturn(easyRandom.nextObject(byte[].class));
        Claims claims = mock(Claims.class);
        when(claims.get("id")).thenReturn(easyRandom.nextObject(String.class));

        when(userDBService.getUserByIdAndRefreshToken(anyString(), anyString())).thenReturn(null);
        when(requestContext.getXUserId()).thenReturn(easyRandom.nextObject(String.class));
        
        doReturn(new Date(new Date().getTime() + 60 * 60 * 10L)).when(claims).getExpiration();
        try (MockedStatic<JwtUtils> theMock = mockStatic(JwtUtils.class)) {
            theMock.when(() -> JwtUtils.validateJWTToken(any(), any())).thenReturn(claims);

            AppResponse<String> result = userService.refresh(easyRandom.nextObject(String.class));
            assertThat(result.isOk()).isFalse();
        }
    }

}