package com.xuxiaoye.api.services;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.dhatim.fastexcel.reader.Row;

import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.RoleMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.client.CRUDDbClient;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.services.db.RoleDBService;
import com.xuxiaoye.api.services.db.mapper.RoleDBMapper;
import com.xuxiaoye.api.services.interfaces.RoleService;
import com.xuxiaoye.api.utils.ExcelHelper;

import java.time.LocalDateTime;

import static com.xuxiaoye.api.client.BaseDbClient.Operator.*;

@Log4j2
public class RoleServiceImpl extends CRUDDbClient<
        Role,
        SearchRoleRequest,
        PagedRoles,
        RoleMapper,
        com.xuxiaoye.api.services.db.dto.entity.Role,
        RoleDBMapper,
        RoleDBService
        > implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleDBService roleDBService;

    public RoleServiceImpl(
            RequestContext requestContext,
            RoleMapper roleMapper,
            RoleDBService roleDBService
    ) {
        this.requestContext = requestContext;
        this.roleMapper = roleMapper;
        this.roleDBService = roleDBService;
    }

    @Override
    public RoleMapper getMapper() {
        return this.roleMapper;
    }

    @Override
    public RoleDBService getDBService() {
        return this.roleDBService;
    }

    @Override
    public LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.Role> buildQuery(
            LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.Role> query,
            SearchRoleRequest searchRoleRequest,
            Pagination pagination
    ) {
        addSortField(
                query,
                pagination,
                sortField -> com.xuxiaoye.api.services.db.dto.entity.Role::getId
        );

        // Todo - Add search fields here
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.Role::getId, searchRoleRequest.getIds());
        addFilter(query, DATETIME_RANGE, com.xuxiaoye.api.services.db.dto.entity.Role::getCreatedAt, searchRoleRequest.getCreatedAts());
        addFilter(query, DATETIME_RANGE, com.xuxiaoye.api.services.db.dto.entity.Role::getUpdatedAt, searchRoleRequest.getUpdatedAts());
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.Role::getCreatedBy, searchRoleRequest.getCreatedBys());
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.Role::getUpdatedBy, searchRoleRequest.getUpdatedBys());

        // Keyword search
        if (org.apache.commons.lang3.StringUtils.isNotBlank(searchRoleRequest.getKeyword())) {
            applyMultiColumnKeyWordFilter(query, searchRoleRequest.getKeyword(),
                    com.xuxiaoye.api.services.db.dto.entity.Role::getId,
                    com.xuxiaoye.api.services.db.dto.entity.Role::getAuthority
            );
        }

        return query;
    }

    @Override
    protected AppResponse<Role> validate(Role role) {
        if (StringUtils.isBlank(role.getAuthority())) {
            return AppResponse.failWithStatus(AppStatus.badRequest("Missing Authority"));
        }
        return AppResponse.ok();
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{
                "ACTION", // A - Add, U - Update , D - Delete
                "ID",
                "AUTHORITY",
                "CREATED BY",
                "CREATED AT",
                "UPDATED BY",
                "UPDATED AT"
        };
    }

    @Override
    protected void writeRow(ExcelHelper.ExcelWriter excelWriter, int rowIdx, int colIdx, Role role) {
        excelWriter.value(rowIdx, colIdx++, "");
        excelWriter.value(rowIdx, colIdx++, role.getId());
        excelWriter.value(rowIdx, colIdx++, role.getAuthority());
        excelWriter.value(rowIdx, colIdx++, role.getCreatedBy());
        excelWriter.value(rowIdx, colIdx++, role.getCreatedAt());
        excelWriter.value(rowIdx, colIdx++, role.getUpdatedBy());
        excelWriter.value(rowIdx, colIdx++, role.getUpdatedAt());
    }

    @Override
    protected Role buildFromRow(String id, int colIdx, Row row) {
        String authority = row.getCell(colIdx++).asString();
        // default columns
        String createdBy = row.getCellAsString(colIdx++).orElse(this.requestContext.getXUserId());
        String createdAt = row.getCellAsString(colIdx++).orElse(LocalDateTime.now().toString());
        String updatedBy = row.getCellAsString(colIdx++).orElse(this.requestContext.getXUserId());
        String updatedAt = row.getCellAsString(colIdx++).orElse(LocalDateTime.now().toString());

        return Role.builder()
                .id(id)
                .authority(authority)
                // default columns
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}