package com.xuxiaoye.api.services.interfaces;

import io.jsonwebtoken.Claims;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.conf.ResourceConfig;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.services.UserServiceImpl;
import com.xuxiaoye.api.services.db.UserDBService;
import com.xuxiaoye.api.services.db.dto.entity.User;
import com.xuxiaoye.api.utils.JwtUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
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

        try (MockedStatic<JwtUtils> theMock = mockStatic(JwtUtils.class)) {
            theMock.when(() -> JwtUtils.validateJWTToken(any(), any())).thenReturn(claims);

            AppResponse<String> result = userService.refresh(easyRandom.nextObject(String.class));
            assertThat(result.isOk()).isFalse();
        }
    }

    @Test
    void testWhenLogoutError() {
        User dbUser = easyRandom.nextObject(User.class);

        when(requestContext.getXUserId()).thenReturn(dbUser.getId());
        when(userDBService.getById(anyString())).thenReturn(dbUser);
        when(userDBService.updateById(any(User.class))).thenReturn(false);

        AppResponse<String> result = userService.logout();
        assertThat(result.isInternalError()).isTrue();
    }

}