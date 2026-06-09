package com.xuxiaoye.api.conf;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.interceptors.JWTAuthenticationFilter;

public class InterceptorConfig implements WebMvcConfigurer {

    private final RequestContext requestContext;

    private final ResourceConfig resourceConfig;

    private final Cache<String, Boolean> cache;

    public InterceptorConfig(
            RequestContext requestContext,
            ResourceConfig resourceConfig,
            @Qualifier("nonceCache") Cache<String, Boolean> cache
    ) {
        this.requestContext = requestContext;
        this.resourceConfig = resourceConfig;
        this.cache = cache;
    }

    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter(requestContext, resourceConfig, cache);
    }
}
