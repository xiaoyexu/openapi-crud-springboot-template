package com.xuxiaoye.api.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.xuxiaoye.api.adapter.server.mapper.StudentMapper;
import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.services.PermissionEvaluatorImpl;
import com.xuxiaoye.api.services.db.StudentAuditDBService;
import com.xuxiaoye.api.services.db.StudentDBService;
import com.xuxiaoye.api.services.db.UserDBService;
import com.xuxiaoye.api.services.interfaces.StudentService;
import com.xuxiaoye.api.services.interfaces.UserService;
import com.xuxiaoye.api.services.StudentServiceImpl;
import com.xuxiaoye.api.services.UserServiceImpl;
import org.springframework.security.access.PermissionEvaluator;

public class ServiceConfig {

    @Bean("P")
    PermissionEvaluator authorizationService(
            @Autowired StudentDBService studentDBService
    ) {
        return new PermissionEvaluatorImpl(studentDBService);
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
    StudentAuditDBService studentAuditDBService() {
        return new StudentAuditDBService();
    }
}
