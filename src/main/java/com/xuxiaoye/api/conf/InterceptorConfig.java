package com.xuxiaoye.api.conf;

import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.interceptors.JWTInterceptor;

public class InterceptorConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;

    private final RequestContext requestContext;

    private final ResourceConfig resourceConfig;

    private final Cache<String, Boolean> cache;

    public InterceptorConfig(
            ObjectMapper objectMapper,
            RequestContext requestContext,
            ResourceConfig resourceConfig,
            @Qualifier("nonceCache") Cache<String, Boolean> cache
    ) {
        this.objectMapper = objectMapper;
        this.requestContext = requestContext;
        this.resourceConfig = resourceConfig;
        this.cache = cache;
    }

    @Bean
    public JWTInterceptor jwtInterceptor() {
        return new JWTInterceptor(requestContext, resourceConfig, cache);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor())
                .addPathPatterns("/**")
                .order(2)
                .excludePathPatterns(Arrays.asList(
                        "/users/login",
                        "/users/refresh",
                        "/api-docs/**",
                        "/swagger*/**"
                ));
    }
}
