package com.xuxiaoye.api.conf;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.PermissionEvaluator;

import com.xuxiaoye.api.adapter.server.mapper.StudentAuditMapper;
import com.xuxiaoye.api.adapter.server.mapper.StudentMapper;
import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.services.db.StudentAuditDBService;
import com.xuxiaoye.api.services.db.StudentDBService;
import com.xuxiaoye.api.services.db.UserDBService;
import com.xuxiaoye.api.services.interfaces.StudentAuditService;
import com.xuxiaoye.api.services.interfaces.StudentService;
import com.xuxiaoye.api.services.interfaces.UserService;
import com.xuxiaoye.api.services.PermissionServiceImpl;
import com.xuxiaoye.api.services.StudentAuditServiceImpl;
import com.xuxiaoye.api.services.StudentServiceImpl;
import com.xuxiaoye.api.services.UserServiceImpl;

public class ServiceConfig {

    @Bean
    public Cache<String, Boolean> nonceCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(500_000)
                .build();
    }

    @Bean("P")
    PermissionEvaluator permissionEvaluator(
            @Autowired StudentDBService studentDBService
    ) {
        return new PermissionServiceImpl(studentDBService);
    }

    @Bean
    UserDBService userDBService() {
        return new UserDBService();
    }

    @Bean
    UserService userService(
            @Autowired RequestContext requestContext,
            @Autowired ResourceConfig resourceConfig,
            @Autowired UserDBService userDBService
    ) {
        return new UserServiceImpl(requestContext, resourceConfig, userDBService);
    }

    @Bean
    StudentDBService studentDBService() {
        return new StudentDBService();
    }

    @Bean
    StudentService studentService(
            @Autowired RequestContext requestContext,
            @Autowired StudentMapper studentMapper,
            @Autowired StudentDBService studentDBService
    ) {
        return new StudentServiceImpl(requestContext, studentMapper, studentDBService);
    }

    @Bean
    StudentAuditService studentAuditService(
            @Autowired StudentAuditMapper studentAuditMapper,
            @Autowired StudentAuditDBService studentAuditDBService
    ) {
        return new StudentAuditServiceImpl(studentAuditMapper, studentAuditDBService);
    }

    @Bean
    StudentAuditDBService studentAuditDBService() {
        return new StudentAuditDBService();
    }
}
