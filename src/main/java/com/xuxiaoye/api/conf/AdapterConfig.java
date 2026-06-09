package com.xuxiaoye.api.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.xuxiaoye.api.adapter.server.*;
import com.xuxiaoye.api.adapter.server.mapper.CommonMapper;
import com.xuxiaoye.api.services.interfaces.*;

public class AdapterConfig {
    @Bean
    UserAdapter userAdapter(
            CommonMapper commonMapper,
            UserService userService
    ) {
        return new UserAdapter(commonMapper, userService);
    }

    @Bean
    UserAuditAdapter userAuditAdapter(
            CommonMapper commonMapper,
            UserAuditService userAuditService
    ) {
        return new UserAuditAdapter(commonMapper, userAuditService);
    }

    @Bean
    StudentAdapter studentAdapter(
            CommonMapper commonMapper,
            StudentService studentService
    ) {
        return new StudentAdapter(commonMapper, studentService);
    }

    @Bean
    StudentAuditAdapter studentAuditAdapter(
            CommonMapper commonMapper,
            StudentAuditService studentAuditService
    ) {
        return new StudentAuditAdapter(commonMapper, studentAuditService);
    }

    @Bean
    RoleAdapter roleAdapter(
            CommonMapper commonMapper,
            RoleService roleService
    ) {
        return new RoleAdapter(commonMapper, roleService);
    }

    @Bean
    RoleAuditAdapter roleAuditAdapter(
            CommonMapper commonMapper,
            RoleAuditService roleAuditService
    ) {
        return new RoleAuditAdapter(commonMapper, roleAuditService);
    }
}
