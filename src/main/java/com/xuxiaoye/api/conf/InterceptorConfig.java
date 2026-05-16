package com.xuxiaoye.api.conf;

import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.interceptors.JWTInterceptor;

public class InterceptorConfig implements WebMvcConfigurer {
    @Value("${bypassTokenCheck}")
    private boolean bypassTokenCheck;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RequestContext requestContext;

    @Autowired
    ResourceConfig resourceConfig;

    @Autowired
    Cache<String, Boolean> cache;

    @Bean
    public JWTInterceptor jwtInterceptor() {
        return new JWTInterceptor(requestContext, resourceConfig, cache, bypassTokenCheck);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor())
                .addPathPatterns("/**")
                .order(2)
                .excludePathPatterns(Arrays.asList(
                        "/user/login",
                        "/api-docs/**",
                        "/swagger*/**"
                ));
    }
}
