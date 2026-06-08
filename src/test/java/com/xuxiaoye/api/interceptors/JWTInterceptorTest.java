package com.xuxiaoye.api.interceptors;

import java.util.Date;

import com.github.benmanes.caffeine.cache.Cache;
import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.common.exceptions.JWTExpiredException;
import com.xuxiaoye.api.conf.ResourceConfig;
import com.xuxiaoye.api.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

class JWTInterceptorTest {
    @Mock
    private RequestContext requestContext;

    @Mock
    private ResourceConfig resourceConfig;

    @Mock
    private Cache<String, Boolean> cache;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Object object;

    @Mock
    private ClientHttpRequestExecution clientHttpRequestExecution;

    @Mock
    Claims claims;

    @Mock
    ClientHttpRequest httpRequest;

    EasyRandom easyRandom = new EasyRandom();

    @BeforeEach
    void setup() {
        requestContext = mock(RequestContext.class);
        resourceConfig = mock(ResourceConfig.class);
        cache = mock(Cache.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        claims = mock(Claims.class);
        clientHttpRequestExecution = mock(ClientHttpRequestExecution.class);
        httpRequest = mock(ClientHttpRequest.class);

        when(cache.getIfPresent(anyString())).thenReturn(false);
    }

    @Nested
    class NoAuthCheckRequired {
        @Test
        public void testPreHandleByPass() throws Exception {
            JWTInterceptor jwtInterceptor = spy(
                    new JWTInterceptor(requestContext, resourceConfig, cache)
            );
            assertTrue(jwtInterceptor.preHandle(request, response, object));
        }
    }

    @Nested
    class RequireAuthCheck {
        @Test
        public void testAllowGetMethodAnyways() throws Exception {
            JWTInterceptor jwtInterceptor = spy(
                    new JWTInterceptor(requestContext, resourceConfig, cache)
            );
            doReturn("").when(request).getHeader(AUTHORIZATION);
            doReturn("GET").when(request).getMethod();
            assertDoesNotThrow(() -> jwtInterceptor.preHandle(request, response, object));
        }

        @Test
        public void testValidToken() throws Exception {
            String token = easyRandom.nextObject(String.class);
            JWTInterceptor jwtInterceptor = spy(
                    new JWTInterceptor(requestContext, resourceConfig, cache)
            );
            doReturn(token).when(request).getHeader(AUTHORIZATION);

            Claims claims = mock(Claims.class);
            when(claims.get("id")).thenReturn(easyRandom.nextObject(String.class));
            when(claims.get("accountName")).thenReturn(easyRandom.nextObject(String.class));
            when(claims.get("roles")).thenReturn(easyRandom.nextObject(String.class));

            doReturn(new Date(new Date().getTime() + 60 * 60 * 10L)).when(claims).getExpiration();
            try (MockedStatic<JwtUtils> theMock = mockStatic(JwtUtils.class)) {
                theMock.when(() -> JwtUtils.validateJWTToken(eq(token), any())).thenReturn(claims);
                assertTrue(jwtInterceptor.preHandle(request, response, object));
            }
        }

        @Test
        public void testExpiredToken() {
            String token = easyRandom.nextObject(String.class);
            JWTInterceptor jwtInterceptor = spy(
                    new JWTInterceptor(requestContext, resourceConfig, cache)
            );
            doReturn(token).when(request).getHeader(AUTHORIZATION);

            Claims claims = mock(Claims.class);
            doReturn(new Date(new Date().getTime() - 60 * 60 * 10L)).when(claims).getExpiration();
            try (MockedStatic<JwtUtils> theMock = mockStatic(JwtUtils.class)) {
                theMock.when(() -> JwtUtils.validateJWTToken(eq(token), any())).thenReturn(claims);
                assertThrows(JWTExpiredException.class, () -> jwtInterceptor.preHandle(request, response, object));
            }
        }
    }
}