package com.xuxiaoye.api.conf;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.PermissionEvaluator;

import com.xuxiaoye.api.adapter.server.mapper.RoleAuditMapper;
import com.xuxiaoye.api.adapter.server.mapper.RoleMapper;
import com.xuxiaoye.api.adapter.server.mapper.StudentAuditMapper;
import com.xuxiaoye.api.adapter.server.mapper.StudentMapper;
import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.services.*;
import com.xuxiaoye.api.services.db.*;
import com.xuxiaoye.api.services.interfaces.*;

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
            @Autowired StudentDBService studentDBService,
            @Autowired RoleDBService roleDBService
    ) {
        return new PermissionServiceImpl(
                studentDBService,
                roleDBService
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
