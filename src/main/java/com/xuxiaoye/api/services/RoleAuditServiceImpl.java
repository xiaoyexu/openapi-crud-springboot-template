package com.xuxiaoye.api.services;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.log4j.Log4j2;

import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.RoleAuditMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.client.CRUDDbClient;
import com.xuxiaoye.api.services.db.RoleAuditDBService;
import com.xuxiaoye.api.services.db.mapper.RoleAuditDBMapper;
import com.xuxiaoye.api.services.interfaces.RoleAuditService;
import com.xuxiaoye.api.utils.ExcelHelper;
import com.xuxiaoye.api.bean.RequestContext;

import static com.xuxiaoye.api.client.BaseDbClient.Operator.*;

@Log4j2
public class RoleAuditServiceImpl extends CRUDDbClient<
        RoleAudit,
        SearchRoleAuditRequest,
        PagedRoleAudits,
        RoleAuditMapper,
        com.xuxiaoye.api.services.db.dto.entity.RoleAudit,
        RoleAuditDBMapper,
        RoleAuditDBService
        > implements RoleAuditService {

    private final RoleAuditMapper roleAuditMapper;
    private final RoleAuditDBService roleAuditDBService;

    public RoleAuditServiceImpl(
            RequestContext requestContext,
            RoleAuditMapper roleAuditMapper,
            RoleAuditDBService roleAuditDBService
    ) {
        this.requestContext = requestContext;
        this.roleAuditMapper = roleAuditMapper;
        this.roleAuditDBService = roleAuditDBService;
    }

    @Override
    public RoleAuditMapper getMapper() {
        return this.roleAuditMapper;
    }

    @Override
    public RoleAuditDBService getDBService() {
        return this.roleAuditDBService;
    }

    @Override
    public LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.RoleAudit> buildQuery(
            LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.RoleAudit> query,
            SearchRoleAuditRequest searchRoleAuditRequest,
            Pagination pagination
    ) {
        addSortField(
                query,
                pagination,
                sortField -> com.xuxiaoye.api.services.db.dto.entity.RoleAudit::getAuditId
        );

        // Todo - Add search fields here
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.RoleAudit::getId, searchRoleAuditRequest.getIds());
        addFilter(query, DATETIME_RANGE, com.xuxiaoye.api.services.db.dto.entity.RoleAudit::getCreatedAt, searchRoleAuditRequest.getCreatedAts());
        addFilter(query, DATETIME_RANGE, com.xuxiaoye.api.services.db.dto.entity.RoleAudit::getUpdatedAt, searchRoleAuditRequest.getUpdatedAts());
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.RoleAudit::getCreatedBy, searchRoleAuditRequest.getCreatedBys());
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.RoleAudit::getUpdatedBy, searchRoleAuditRequest.getUpdatedBys());

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
    protected void writeRow(ExcelHelper.ExcelWriter excelWriter, int rowIdx, int colIdx, RoleAudit roleAudit) {
        excelWriter.value(rowIdx, colIdx++, "");
        excelWriter.value(rowIdx, colIdx++, roleAudit.getId());
        excelWriter.value(rowIdx, colIdx++, roleAudit.getCreatedBy());
        excelWriter.value(rowIdx, colIdx++, roleAudit.getCreatedAt());
        excelWriter.value(rowIdx, colIdx++, roleAudit.getUpdatedBy());
        excelWriter.value(rowIdx, colIdx++, roleAudit.getUpdatedAt());
    }
}