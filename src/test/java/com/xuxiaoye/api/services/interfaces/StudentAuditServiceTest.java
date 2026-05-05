package com.xuxiaoye.api.services.interfaces;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.ActiveProfiles;

import com.xuxiaoye.api.adapter.api.server.dto.SearchStudentAuditRequest;
import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.FileResponse;
import com.xuxiaoye.api.services.StudentAuditServiceImpl;
import com.xuxiaoye.api.services.db.StudentAuditDBService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
class StudentAuditServiceTest {
    @InjectMocks
    StudentAuditServiceImpl studentAuditService;

    @Mock
    StudentAuditDBService studentAuditDBService;

    @Test
    void exportStudentAuditError() {
        doThrow(new RuntimeException()).when(studentAuditDBService).page(any(Page.class), any(LambdaQueryWrapper.class));
        AppResponse<FileResponse> response = studentAuditService.exportStudentAudits(
                new SearchStudentAuditRequest(), Pagination.of(1, 1, "")
        );
        assertThat(response.isInternalError()).isTrue();
    }

}
