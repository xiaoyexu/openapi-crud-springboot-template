package com.xuxiaoye.api.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.interceptors.RequestContextInterceptor;

public class WebConfig {

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    RequestContext requestContext() {
        return RequestContext.builder().build();
    }

    @Bean
    public RequestContextInterceptor requestContextInterceptor(@Autowired RequestContext requestContext) {
        return new RequestContextInterceptor(requestContext);
    }

    @Bean
    public WebMvcConfigurer registerInterceptors(@Autowired RequestContextInterceptor requestContextInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@Autowired(required = true) InterceptorRegistry registry) {
                registry.addInterceptor(requestContextInterceptor)
                        .addPathPatterns("/**")
                        .excludePathPatterns("/health").order(1);
            }
        };
    }

}
