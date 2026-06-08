package com.xuxiaoye.api.services;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import lombok.extern.log4j.Log4j2;

import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.StudentAuditMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.client.CRUDDbClient;
import com.xuxiaoye.api.services.db.StudentAuditDBService;
import com.xuxiaoye.api.services.db.mapper.StudentAuditDBMapper;
import com.xuxiaoye.api.services.interfaces.StudentAuditService;
import com.xuxiaoye.api.utils.ExcelHelper;

import static com.xuxiaoye.api.client.BaseDbClient.Operator.*;

@Log4j2
public class StudentAuditServiceImpl extends CRUDDbClient<
        StudentAudit,
        SearchStudentAuditRequest,
        PagedStudentAudits,
        StudentAuditMapper,
        com.xuxiaoye.api.services.db.dto.entity.StudentAudit,
        StudentAuditDBMapper,
        StudentAuditDBService
        > implements StudentAuditService {

    private final StudentAuditMapper studentAuditMapper;
    private final StudentAuditDBService studentAuditDBService;

    public StudentAuditServiceImpl(
            RequestContext requestContext,
            StudentAuditMapper studentAuditMapper,
            StudentAuditDBService studentAuditDBService
    ) {
        this.requestContext = requestContext;
        this.studentAuditMapper = studentAuditMapper;
        this.studentAuditDBService = studentAuditDBService;
    }

    @Override
    public StudentAuditMapper getMapper() {
        return this.studentAuditMapper;
    }

    @Override
    public StudentAuditDBService getDBService() {
        return this.studentAuditDBService;
    }

    @Override
    public LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.StudentAudit> buildQuery(
            LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.StudentAudit> query,
            SearchStudentAuditRequest searchStudentAuditRequest,
            Pagination pagination
    ) {
        addSortField(
                query,
                pagination,
                sortField -> com.xuxiaoye.api.services.db.dto.entity.StudentAudit::getId
        );

        // Todo - Add search fields here
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.StudentAudit::getId, searchStudentAuditRequest.getIds());
        addFilter(query, DATETIME_RANGE, com.xuxiaoye.api.services.db.dto.entity.StudentAudit::getCreatedAt, searchStudentAuditRequest.getCreatedAts());
        addFilter(query, DATETIME_RANGE, com.xuxiaoye.api.services.db.dto.entity.StudentAudit::getUpdatedAt, searchStudentAuditRequest.getUpdatedAts());
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.StudentAudit::getCreatedBy, searchStudentAuditRequest.getCreatedBys());
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.StudentAudit::getUpdatedBy, searchStudentAuditRequest.getUpdatedBys());

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
    protected void writeRow(ExcelHelper.ExcelWriter excelWriter, int rowIdx, int colIdx, StudentAudit studentAudit) {
        excelWriter.value(rowIdx, colIdx++, "");
        excelWriter.value(rowIdx, colIdx++, studentAudit.getId());
        // Add data column here
        // excelHelper.value(row.get(), col.getAndIncrement(), studentAudit.getXXX());
        excelWriter.value(rowIdx, colIdx++, studentAudit.getCreatedBy());
        excelWriter.value(rowIdx, colIdx++, studentAudit.getCreatedAt());
        excelWriter.value(rowIdx, colIdx++, studentAudit.getUpdatedBy());
        excelWriter.value(rowIdx, colIdx++, studentAudit.getUpdatedAt());
    }
}