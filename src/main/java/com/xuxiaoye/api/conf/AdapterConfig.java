package com.xuxiaoye.api.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.xuxiaoye.api.adapter.server.*;
import com.xuxiaoye.api.adapter.server.mapper.CommonMapper;
import com.xuxiaoye.api.services.interfaces.*;

public class AdapterConfig {
    @Bean
    UserAdapter userAdapter(
            @Autowired CommonMapper commonMapper,
            @Autowired UserService userService
    ) {
        return new UserAdapter(commonMapper, userService);
    }

    @Bean
    StudentAdapter studentAdapter(
            @Autowired CommonMapper commonMapper,
            @Autowired StudentService studentService
    ) {
        return new StudentAdapter(commonMapper, studentService);
    }

    @Bean
    StudentAuditAdapter studentAuditAdapter(
            @Autowired CommonMapper commonMapper,
            @Autowired StudentAuditService studentAuditService
    ) {
        return new StudentAuditAdapter(commonMapper, studentAuditService);
    }

    @Bean
    RoleAdapter roleAdapter(
            @Autowired CommonMapper commonMapper,
            @Autowired RoleService roleService
    ) {
        return new RoleAdapter(commonMapper, roleService);
    }

    @Bean
    RoleAuditAdapter roleAuditAdapter(
            @Autowired CommonMapper commonMapper,
            @Autowired RoleAuditService roleAuditService
    ) {
        return new RoleAuditAdapter(commonMapper, roleAuditService);
    }
}
