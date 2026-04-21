package com.xuxiaoye.api.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import com.xuxiaoye.api.adapter.server.StudentAdapter;
import com.xuxiaoye.api.adapter.server.mapper.CommonMapper;
import com.xuxiaoye.api.services.interfaces.StudentService;

public class AdapterConfig {
    @Bean
    StudentAdapter studentAdapter(
            @Autowired CommonMapper commonMapper,
            @Autowired StudentService studentService
    ) {
        return new StudentAdapter(commonMapper, studentService);
    }
}
