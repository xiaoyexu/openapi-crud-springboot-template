package com.xuxiaoye.api.conf;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.PermissionEvaluator;

import com.xuxiaoye.api.adapter.server.mapper.*;
import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.services.*;
import com.xuxiaoye.api.services.db.*;
import com.xuxiaoye.api.services.interfaces.*;

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
            @Autowired @Qualifier("permissionCache") Cache<String, Boolean> cache,
            @Autowired StudentDBService studentDBService,
            @Autowired RoleDBService roleDBService,
            @Autowired UserDBService userDBService
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
            @Autowired RequestContext requestContext,
            @Autowired ResourceConfig resourceConfig,
            @Autowired UserMapper userMapper,
            @Autowired UserDBService userDBService
    ) {
        return new UserServiceImpl(requestContext, resourceConfig, userMapper, userDBService);
    }

    @Bean
    UserAuditDBService userAuditDBService() {
        return new UserAuditDBService();
    }

    @Bean
    UserAuditService userAuditService(
            @Autowired RequestContext requestContext,
            @Autowired UserAuditMapper userAuditMapper,
            @Autowired UserAuditDBService userAuditDBService
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
            @Autowired RequestContext requestContext,
            @Autowired StudentMapper studentMapper,
            @Autowired StudentDBService studentDBService
    ) {
        return new StudentServiceImpl(requestContext, studentMapper, studentDBService);
    }

    @Bean
    StudentAuditService studentAuditService(
            @Autowired RequestContext requestContext,
            @Autowired StudentAuditMapper studentAuditMapper,
            @Autowired StudentAuditDBService studentAuditDBService
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
            @Autowired RequestContext requestContext,
            @Autowired RoleMapper roleMapper,
            @Autowired RoleDBService roleDBService
    ) {
        return new RoleServiceImpl(requestContext, roleMapper, roleDBService);
    }

    @Bean
    RoleAuditDBService roleAuditDBService() {
        return new RoleAuditDBService();
    }

    @Bean
    RoleAuditService roleAuditService(
            @Autowired RequestContext requestContext,
            @Autowired RoleAuditMapper roleAuditMapper,
            @Autowired RoleAuditDBService roleAuditDBService
    ) {
        return new RoleAuditServiceImpl(
                requestContext,
                roleAuditMapper,
                roleAuditDBService
        );
    }
}
