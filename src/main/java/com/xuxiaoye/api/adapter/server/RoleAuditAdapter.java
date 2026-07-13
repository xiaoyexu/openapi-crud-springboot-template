package com.xuxiaoye.api.adapter.server;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import com.xuxiaoye.api.adapter.api.server.RoleAuditsApiDelegate;
import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.CommonMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.resp.FileResponse;
import com.xuxiaoye.api.services.interfaces.RoleAuditService;

@Log4j2
public class RoleAuditAdapter implements RoleAuditsApiDelegate {

    private final CommonMapper commonMapper;
    private final RoleAuditService roleAuditService;

    public RoleAuditAdapter(
            CommonMapper commonMapper,
            RoleAuditService roleAuditService
    ) {
        this.commonMapper = commonMapper;
        this.roleAuditService = roleAuditService;
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, #roleAuditId, 'roleAudit', 'get') or @P.hasPermission(authentication, #roleAuditId, 'roleAudit', 'get_own')")
    public ResponseEntity<GetRoleAuditResponse> getSingleRoleAudit(
            String xTraceID,
            String roleAuditId,
            String authorization
    ) {
        return this.roleAuditService.get(roleAuditId)
                .toResponseEntity(
                        data -> GetRoleAuditResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> GetRoleAuditResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'roleAudit', 'search')")
    public ResponseEntity<SearchRoleAuditResponse> searchRoleAudits(
            String xTraceID,
            SearchRoleAuditRequest searchRoleAuditRequest,
            String authorization,
            Integer limit,
            Integer offset,
            String sortBy
    ) {
        return this.roleAuditService.search(searchRoleAuditRequest, Pagination.of(offset, limit, sortBy))
                .toResponseEntity(
                        data -> SearchRoleAuditResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> SearchRoleAuditResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'roleAudit', 'export')")
    public ResponseEntity<org.springframework.core.io.Resource> exportRoleAudits(
            String xTraceID,
            SearchRoleAuditRequest searchRoleAuditRequest,
            String authorization,
            Integer limit,
            Integer offset,
            String sortBy
    ) {
        return this.roleAuditService.exportData(searchRoleAuditRequest, Pagination.of(offset, limit, sortBy), "RoleAudits")
                .toFileResponseEntity();
    }
}