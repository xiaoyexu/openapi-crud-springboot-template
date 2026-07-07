package com.xuxiaoye.api.services.interfaces;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xuxiaoye.api.adapter.api.server.dto.SearchStudentAuditRequest;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.FileResponse;
import com.xuxiaoye.api.services.StudentAuditServiceImpl;
import com.xuxiaoye.api.services.db.StudentAuditDBService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class StudentAuditServiceTest {
    @InjectMocks
    StudentAuditServiceImpl studentAuditService;

    @Mock
    StudentAuditDBService studentAuditDBService;

    @Test
    void exportStudentAuditError() {
        doThrow(new RuntimeException()).when(studentAuditDBService).page(any(Page.class), any(LambdaQueryWrapper.class));
        AppResponse<FileResponse> response = studentAuditService.exportData(
                new SearchStudentAuditRequest(), Pagination.of(1, 1, ""),
                "StudentAudits"
        );
        assertThat(response.isInternalError()).isTrue();
    }

}
