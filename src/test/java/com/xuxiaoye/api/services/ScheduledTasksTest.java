package com.xuxiaoye.api.services;


import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.ActiveProfiles;

import com.xuxiaoye.api.adapter.api.server.dto.SearchStudentRequest;
import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.conf.ResourceConfig;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.services.interfaces.StudentService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = Application.class
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
class ScheduledTasksTest {
    @Autowired
    ResourceConfig resourceConfig;

    @Mock
    StudentService studentService;

    ScheduledTasks scheduledTasks;

    @BeforeAll
    void setup() {
        scheduledTasks = new ScheduledTasks(resourceConfig, studentService);
    }

    @Test
    public void autoCancelOvertimeOUnpaidRegistrations() {
        when(studentService.searchStudent(any(SearchStudentRequest.class), any(Pagination.class))).thenReturn(AppResponse.ok());
        assertDoesNotThrow(() -> scheduledTasks.autoCancelOvertimeOUnpaidRegistrations());
    }
}