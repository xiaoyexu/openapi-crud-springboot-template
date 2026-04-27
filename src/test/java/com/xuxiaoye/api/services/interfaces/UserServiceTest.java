package com.xuxiaoye.api.services.interfaces;

import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.services.UserServiceImpl;
import com.xuxiaoye.api.services.db.UserDBService;
import lombok.extern.log4j.Log4j2;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.ActiveProfiles;

import com.xuxiaoye.api.Application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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

    @Test
    void testWhenInvalidAccess() {
        when(userDBService.getUserByIdAndRefreshToken(anyString(),anyString())).thenReturn(null);
        when(requestContext.getXUserId()).thenReturn(easyRandom.nextObject(String.class));

        AppResponse<String> result = userService.refresh(easyRandom.nextObject(String.class));
        assertThat(result.isOk()).isFalse();
    }

}