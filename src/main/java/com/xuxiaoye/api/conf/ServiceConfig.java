package com.xuxiaoye.api.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.xuxiaoye.api.adapter.server.mapper.StudentMapper;
import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.services.db.StudentAuditDBService;
import com.xuxiaoye.api.services.db.StudentDBService;
import com.xuxiaoye.api.services.StudentServiceImpl;
import com.xuxiaoye.api.services.interfaces.StudentService;

public class ServiceConfig {
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
