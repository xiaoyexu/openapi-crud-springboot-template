package com.xuxiaoye.api.services;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.log4j.Log4j2;

import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.UserAuditMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.client.CRUDDbClient;
import com.xuxiaoye.api.services.db.UserAuditDBService;
import com.xuxiaoye.api.services.db.mapper.UserAuditDBMapper;
import com.xuxiaoye.api.services.interfaces.UserAuditService;
import com.xuxiaoye.api.utils.ExcelHelper;
import com.xuxiaoye.api.bean.RequestContext;

import static com.xuxiaoye.api.client.BaseDbClient.Operator.*;

@Log4j2
public class UserAuditServiceImpl extends CRUDDbClient<
        String,
        UserAudit,
        SearchUserAuditRequest,
        PagedUserAudits,
        UserAuditMapper,
        com.xuxiaoye.api.services.db.dto.entity.UserAudit,
        UserAuditDBMapper,
        UserAuditDBService
        > implements UserAuditService {

    // ========== Exception Handling & ifOk() Pattern Guidelines ==========
    //
    // Error Handling Strategy:
    // 1. Business logic failures -> return AppResponse.failWithStatus()
    // 2. Database errors -> automatically wrapped by handleDbCall()
    // 3. AppException thrown during processing -> caught by BaseDbClient
    //
    // Built-in Functionality (inherited from CRUDDbClient):
    // - exportData(): uses ifOk() pattern for elegant Excel export with error handling
    // - importData(): uses ifOkElse() in handleRow() for CRUD operation chaining
    // ========================================================================

    private final UserAuditMapper userAuditMapper;
    private final UserAuditDBService userAuditDBService;

    public UserAuditServiceImpl(
            RequestContext requestContext,
            UserAuditMapper userAuditMapper,
            UserAuditDBService userAuditDBService
    ) {
        this.requestContext = requestContext;
        this.userAuditMapper = userAuditMapper;
        this.userAuditDBService = userAuditDBService;
    }

    @Override
    public UserAuditMapper getMapper() {
        return this.userAuditMapper;
    }

    @Override
    public UserAuditDBService getDBService() {
        return this.userAuditDBService;
    }

    @Override
    public LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.UserAudit> buildQuery(
            LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.UserAudit> query,
            SearchUserAuditRequest searchUserAuditRequest,
            Pagination pagination
    ) {
        addSortField(
                query,
                pagination,
                sortField -> com.xuxiaoye.api.services.db.dto.entity.UserAudit::getId
        );

        // Todo - Add search fields here
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.UserAudit::getId, searchUserAuditRequest.getIds());
        addFilter(query, DATETIME_RANGE, com.xuxiaoye.api.services.db.dto.entity.UserAudit::getCreatedAt, searchUserAuditRequest.getCreatedAts());
        addFilter(query, DATETIME_RANGE, com.xuxiaoye.api.services.db.dto.entity.UserAudit::getUpdatedAt, searchUserAuditRequest.getUpdatedAts());
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.UserAudit::getCreatedBy, searchUserAuditRequest.getCreatedBys());
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.UserAudit::getUpdatedBy, searchUserAuditRequest.getUpdatedBys());

        return query;
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{
                "ACTION", // A - Add, U - Update , D - Delete
                "ID",
                // Add data column here
                // "XXX",
                "CREATED BY",
                "CREATED AT",
                "UPDATED BY",
                "UPDATED AT"
        };
    }

    @Override
    protected void writeRow(ExcelHelper.ExcelWriter excelWriter, int rowIdx, int colIdx, UserAudit userAudit) {
        excelWriter.value(rowIdx, colIdx++, "");
        excelWriter.value(rowIdx, colIdx++, userAudit.getId());
        // Add data column here
        // excelHelper.value(row.get(), col.getAndIncrement(), userAudit.getAccountName()());
        excelWriter.value(rowIdx, colIdx++, userAudit.getCreatedBy());
        excelWriter.value(rowIdx, colIdx++, userAudit.getCreatedAt());
        excelWriter.value(rowIdx, colIdx++, userAudit.getUpdatedBy());
        excelWriter.value(rowIdx, colIdx++, userAudit.getUpdatedAt());
    }
}