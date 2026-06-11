package com.xuxiaoye.api.adapter.server;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import com.xuxiaoye.api.adapter.api.server.UserAuditsApiDelegate;
import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.CommonMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.services.interfaces.UserAuditService;

@Log4j2
public class UserAuditAdapter implements UserAuditsApiDelegate {

    private final CommonMapper commonMapper;
    private final UserAuditService userAuditService;

    public UserAuditAdapter(
            CommonMapper commonMapper,
            UserAuditService userAuditService
    ) {
        this.commonMapper = commonMapper;
        this.userAuditService = userAuditService;
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, #userAuditId, 'userAudit', 'get') or @P.hasPermission(authentication, #userAuditId, 'userAudit', 'get_own')")
    public ResponseEntity<GetUserAuditResponse> getSingleUserAudit(
            String xTraceID,
            String authorization,
            String userAuditId
    ) {
        return this.userAuditService.get(userAuditId)
                .toResponseEntity(
                        data -> GetUserAuditResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> GetUserAuditResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'userAudit', 'search')")
    public ResponseEntity<SearchUserAuditResponse> searchUserAudits(
            String xTraceID,
            String authorization,
            SearchUserAuditRequest searchUserAuditRequest,
            Integer limit,
            Integer offset,
            String sortBy
    ) {
        return this.userAuditService.search(searchUserAuditRequest, Pagination.of(offset, limit, sortBy))
                .toResponseEntity(
                        data -> SearchUserAuditResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> SearchUserAuditResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'userAudit', 'export')")
    public ResponseEntity<org.springframework.core.io.Resource> exportUserAudits(
            String xTraceID,
            String authorization,
            SearchUserAuditRequest searchUserAuditRequest,
            Integer limit,
            Integer offset,
            String sortBy
    ) {
        return this.userAuditService.exportData(searchUserAuditRequest, Pagination.of(offset, limit, sortBy), "UserAudits")
                .toFileResponseEntity();
    }
}