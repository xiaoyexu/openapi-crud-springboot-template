package com.xuxiaoye.api.conf;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.PermissionEvaluator;

import com.xuxiaoye.api.adapter.server.mapper.*;
import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.services.*;
import com.xuxiaoye.api.services.db.*;
import com.xuxiaoye.api.services.interfaces.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class ServiceConfig {

    @Bean("nonceCache")
    public Cache<String, Boolean> nonceCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(500_000)
                .build();
    }

    @Bean("permissionCache")
    public Cache<String, Boolean> permissionCache() {
        return Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    @Bean("P")
    PermissionEvaluator permissionEvaluator(
            @Qualifier("permissionCache") Cache<String, Boolean> cache,
            StudentDBService studentDBService,
            RoleDBService roleDBService,
            UserDBService userDBService
    ) {
        return new PermissionServiceImpl(
                cache,
                studentDBService,
                roleDBService,
                userDBService
        );
    }

    @Bean
    UserDBService userDBService() {
        return new UserDBService();
    }

    @Bean
    UserService userService(
            RequestContext requestContext,
            ResourceConfig resourceConfig,
            UserMapper userMapper,
            UserDBService userDBService,
            PasswordEncoder passwordEncoder
    ) {
        return new UserServiceImpl(requestContext, resourceConfig, userMapper, userDBService, passwordEncoder);
    }

    @Bean
    UserAuditDBService userAuditDBService() {
        return new UserAuditDBService();
    }

    @Bean
    UserAuditService userAuditService(
            RequestContext requestContext,
            UserAuditMapper userAuditMapper,
            UserAuditDBService userAuditDBService
    ) {
        return new UserAuditServiceImpl(
                requestContext,
                userAuditMapper,
                userAuditDBService
        );
    }

    @Bean
    StudentDBService studentDBService() {
        return new StudentDBService();
    }

    @Bean
    StudentService studentService(
            RequestContext requestContext,
            StudentMapper studentMapper,
            StudentDBService studentDBService
    ) {
        return new StudentServiceImpl(requestContext, studentMapper, studentDBService);
    }

    @Bean
    StudentAuditService studentAuditService(
            RequestContext requestContext,
            StudentAuditMapper studentAuditMapper,
            StudentAuditDBService studentAuditDBService
    ) {
        return new StudentAuditServiceImpl(requestContext, studentAuditMapper, studentAuditDBService);
    }

    @Bean
    StudentAuditDBService studentAuditDBService() {
        return new StudentAuditDBService();
    }

    @Bean
    RoleDBService roleDBService() {
        return new RoleDBService();
    }

    @Bean
    RoleService roleService(
            RequestContext requestContext,
            RoleMapper roleMapper,
            RoleDBService roleDBService
    ) {
        return new RoleServiceImpl(requestContext, roleMapper, roleDBService);
    }

    @Bean
    RoleAuditDBService roleAuditDBService() {
        return new RoleAuditDBService();
    }

    @Bean
    RoleAuditService roleAuditService(
            RequestContext requestContext,
            RoleAuditMapper roleAuditMapper,
            RoleAuditDBService roleAuditDBService
    ) {
        return new RoleAuditServiceImpl(
                requestContext,
                roleAuditMapper,
                roleAuditDBService
        );
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
