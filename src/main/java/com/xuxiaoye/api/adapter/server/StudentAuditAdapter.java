package com.xuxiaoye.api.adapter.server;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import com.xuxiaoye.api.adapter.api.server.StudentAuditsApiDelegate;
import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.CommonMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.resp.FileResponse;
import com.xuxiaoye.api.services.interfaces.StudentAuditService;

@Log4j2
public class StudentAuditAdapter implements StudentAuditsApiDelegate {

    private final CommonMapper commonMapper;
    private final StudentAuditService studentAuditService;

    public StudentAuditAdapter(
            CommonMapper commonMapper,
            StudentAuditService studentAuditService
    ) {
        this.commonMapper = commonMapper;
        this.studentAuditService = studentAuditService;
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, #studentAuditId, 'studentAudit', 'get') or @P.hasPermission(authentication, #studentAuditId, 'studentAudit', 'get_own')")
    public ResponseEntity<GetStudentAuditResponse> getSingleStudentAudit(
            String xTraceID,
            String authorization,
            String studentAuditId
    ) {
        return this.studentAuditService.get(studentAuditId)
                .toResponseEntity(
                        data -> GetStudentAuditResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> GetStudentAuditResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'studentAudit', 'search')")
    public ResponseEntity<SearchStudentAuditResponse> searchStudentAudits(
            String xTraceID,
            String authorization,
            SearchStudentAuditRequest searchStudentAuditRequest,
            Integer limit,
            Integer offset,
            String sortBy
    ) {
        return this.studentAuditService.search(searchStudentAuditRequest, Pagination.of(offset, limit, sortBy))
                .toResponseEntity(
                        data -> SearchStudentAuditResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> SearchStudentAuditResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'studentAudit', 'export')")
    public ResponseEntity<org.springframework.core.io.Resource> exportStudentAudits(
            String xTraceID,
            String authorization,
            SearchStudentAuditRequest searchStudentAuditRequest,
            Integer limit,
            Integer offset,
            String sortBy
    ) {
        return this.studentAuditService.exportData(searchStudentAuditRequest, Pagination.of(offset, limit, sortBy), "StudentAudits")
                .toFileResponseEntity();
    }
}