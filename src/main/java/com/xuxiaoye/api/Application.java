package com.xuxiaoye.api;

import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.xuxiaoye.api.common.exceptions.GlobalExceptionHandler;
import com.xuxiaoye.api.conf.*;
import com.xuxiaoye.api.interceptors.TableAuditLogInterceptor;

@SpringBootApplication
@Import({
        AdapterConfig.class,
        ServiceConfig.class,
        SwaggerConfig.class,
        MybatisPlusConfig.class,
        TableAuditLogInterceptor.class,
        WebConfig.class,
        WebSecurityConfig.class,
        GlobalExceptionHandler.class,
        ResourceConfig.class,
        InterceptorConfig.class
})
@Log4j2
@EnableMethodSecurity
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {

        if (log.isDebugEnabled()) {
            ApplicationContext applicationContext = event.getApplicationContext();

            RequestMappingHandlerMapping requestMappingHandlerMapping = applicationContext
                    .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);

            Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
            map.forEach((key,
                         value) -> log.debug("Endpoint -> [{}]: [{}]", key, value));
        }
    }
}
