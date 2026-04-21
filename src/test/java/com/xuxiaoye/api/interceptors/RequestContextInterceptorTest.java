package com.xuxiaoye.api.interceptors;

import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.constant.CommonConstants;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RequestContextInterceptorTest {
    @Mock
    Object handler;

    @Test
    void testRequestContextInterceptor() {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);

        RequestContext requestContext = RequestContext.builder().build();
        RequestContextInterceptor interceptor = new RequestContextInterceptor(requestContext);
        when(httpServletRequest.getHeader(any(String.class))).thenReturn("header");
        when(httpServletRequest.getHeaderNames()).thenReturn(Collections.enumeration(List.of("1", "2")));
        assertDoesNotThrow(() -> {
            interceptor.preHandle(httpServletRequest, httpServletResponse, handler);
        });

        when(httpServletRequest.getHeader(any(String.class))).thenReturn(null);
        assertDoesNotThrow(() -> {
            interceptor.preHandle(httpServletRequest, httpServletResponse, handler);
        });
    }

    @Test
    void testIntercept() {
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(httpRequest.getHeaders()).thenReturn(headers);

        ClientHttpRequestExecution clientHttpRequestExecution = mock(ClientHttpRequestExecution.class);
        RequestContext requestContext = RequestContext.builder().build();
        RequestContextInterceptor interceptor = new RequestContextInterceptor(requestContext);


        when(headers.get(any(String.class))).thenReturn(List.of("header"));
        assertDoesNotThrow(() -> {
            interceptor.intercept(httpRequest, new byte[]{}, clientHttpRequestExecution);
        });

        when(headers.get(any(String.class))).thenReturn(null);
        assertDoesNotThrow(() -> {
            interceptor.intercept(httpRequest, new byte[]{}, clientHttpRequestExecution);
        });
    }

    @Test
    void testInterceptContextFromRequestAttribute() {
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(httpRequest.getHeaders()).thenReturn(headers);

        ClientHttpRequestExecution clientHttpRequestExecution = mock(ClientHttpRequestExecution.class);
        RequestContext requestContext = RequestContext.builder().build();
        RequestContextInterceptor interceptor = new RequestContextInterceptor(requestContext);

        RequestAttributes attributes = mock(RequestAttributes.class);

        RequestContext attributeContainer = RequestContext.builder().build();

        when(attributes.getAttribute(CommonConstants.REQ_CTX_BEAN_NAME, RequestAttributes.SCOPE_REQUEST)).thenReturn(attributeContainer);
        try (MockedStatic<RequestContextHolder> someClassMockedStatic = mockStatic(RequestContextHolder.class)) {
            someClassMockedStatic.when(() -> RequestContextHolder.getRequestAttributes()).thenReturn(attributes);

            assertDoesNotThrow(() -> {
                interceptor.intercept(httpRequest, new byte[]{}, clientHttpRequestExecution);
            });
        }
    }

    @Test
    void testInterceptContextFromServletRequestAttributes() {
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(httpRequest.getHeaders()).thenReturn(headers);

        ClientHttpRequestExecution clientHttpRequestExecution = mock(ClientHttpRequestExecution.class);
        RequestContext requestContext = RequestContext.builder().build();
        RequestContextInterceptor interceptor = new RequestContextInterceptor(requestContext);

        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);

        RequestContext attributeContainer = RequestContext.builder().build();

        HttpServletRequest httpRequestInAttribute = mock(HttpServletRequest.class);
        when(attributes.getRequest()).thenReturn(httpRequestInAttribute);
        when(httpRequestInAttribute.getAttribute(CommonConstants.REQ_CTX_BEAN_NAME)).thenReturn(attributeContainer);

        try (MockedStatic<RequestContextHolder> someClassMockedStatic = mockStatic(RequestContextHolder.class)) {
            someClassMockedStatic.when(() -> RequestContextHolder.getRequestAttributes()).thenReturn(attributes);

            assertDoesNotThrow(() -> {
                interceptor.intercept(httpRequest, new byte[]{}, clientHttpRequestExecution);
            });
        }
    }

    @Test
    void testInterceptNullContextFromRequestAttribute() {
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(httpRequest.getHeaders()).thenReturn(headers);

        ClientHttpRequestExecution clientHttpRequestExecution = mock(ClientHttpRequestExecution.class);
        RequestContext requestContext = RequestContext.builder().build();
        RequestContextInterceptor interceptor = new RequestContextInterceptor(requestContext);

        try (MockedStatic<RequestContextHolder> someClassMockedStatic = mockStatic(RequestContextHolder.class)) {
            someClassMockedStatic.when(() -> RequestContextHolder.getRequestAttributes()).thenReturn(null);

            assertDoesNotThrow(() -> {
                interceptor.intercept(httpRequest, new byte[]{}, clientHttpRequestExecution);
            });
        }
    }

}