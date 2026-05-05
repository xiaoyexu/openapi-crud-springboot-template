package com.xuxiaoye.api.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;

import com.xuxiaoye.api.adapter.api.server.dto.StudentAudit;
import com.xuxiaoye.api.adapter.api.server.dto.PagedStudentAudits;
import com.xuxiaoye.api.adapter.api.server.dto.SearchStudentAuditRequest;
import com.xuxiaoye.api.adapter.server.mapper.StudentAuditMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.client.BaseDbClient;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.resp.FileResponse;
import com.xuxiaoye.api.services.db.StudentAuditDBService;
import com.xuxiaoye.api.services.interfaces.StudentAuditService;
import com.xuxiaoye.api.utils.ExcelHelper;

import static com.xuxiaoye.api.client.BaseDbClient.Operator.*;
import static com.xuxiaoye.api.utils.DateTimeUtils.*;

@Log4j2
public class StudentAuditServiceImpl extends BaseDbClient implements StudentAuditService {

    private final StudentAuditMapper studentAuditMapper;
    private final StudentAuditDBService studentAuditDBService;

    public StudentAuditServiceImpl(
            StudentAuditMapper studentAuditMapper,
            StudentAuditDBService studentAuditDBService
    ) {
        this.studentAuditMapper = studentAuditMapper;
        this.studentAuditDBService = studentAuditDBService;
    }

    protected StudentAudit buildStudentAudit(com.xuxiaoye.api.services.db.dto.entity.StudentAudit dbStudentAudit) {
        // Add logic here in case need to add more field values on dto object
        return this.studentAuditMapper.map(dbStudentAudit);
    }

    @Override
    public AppResponse<PagedStudentAudits> searchStudentAudit(SearchStudentAuditRequest searchStudentAuditRequest, Pagination pagination) {
        return handleDbCall(() -> {
            Page<com.xuxiaoye.api.services.db.dto.entity.StudentAudit> page = new Page<>(pagination.getOffset(), pagination.getLimit());
            LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.StudentAudit> query = new LambdaQueryWrapper<>();

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

            Page<com.xuxiaoye.api.services.db.dto.entity.StudentAudit> studentAuditPage = this.studentAuditDBService.page(page, query);
            PagedStudentAudits pagedStudentAudits = new PagedStudentAudits(
                    page.getTotal(),
                    this.studentAuditMapper.map(studentAuditPage.getRecords())
                    // Or
                    // studentAuditPage.getRecords().stream().map(this::buildGenerationJob).toList()
            );
            return AppResponse.okWithData(pagedStudentAudits);
        });
    }

    @Override
    public AppResponse<StudentAudit> getStudentAudit(String id) {
        return handleDbCall(() -> {
            com.xuxiaoye.api.services.db.dto.entity.StudentAudit dbStudentAudit = this.studentAuditDBService.getById(id);
            if (dbStudentAudit == null) {
                return AppResponse.failWithStatus(AppStatus.notFound());
            }
            return AppResponse.okWithData(this.buildStudentAudit(dbStudentAudit));
        });
    }

    @Override
    public AppResponse<FileResponse> exportStudentAudits(SearchStudentAuditRequest searchStudentAuditRequest, Pagination pagination) {
        AppResponse<PagedStudentAudits> pagedStudentAuditsAppResponse = this.searchStudentAudit(searchStudentAuditRequest, pagination);
        if (!pagedStudentAuditsAppResponse.isOk()) {
            return AppResponse.failWithStatus(pagedStudentAuditsAppResponse.getStatus());
        }

        ExcelHelper.ExcelWriter excelHelper =  ExcelHelper.getWriter();
        excelHelper
                .newWorkbook("StudentAudit", "1.0")
                .newWorkSheet("StudentAudit");

        String[] headers = new String[]{
                "ACTION", // A - Add, U - Update , D - Delete
                "ID",
                // Add data column here
                // "XXX",
                "CREATED BY",
                "CREATED AT",
                "UPDATED BY",
                "UPDATED AT"
        };
        IntStream.range(0, headers.length).forEach(idx -> {
            excelHelper.value(0, idx, headers[idx]);
        });

        List<StudentAudit> studentAudits = pagedStudentAuditsAppResponse.getData().getData();
        IntStream.range(0, studentAudits.size()).forEach(idx -> {
            StudentAudit studentAudit = studentAudits.get(idx);
            int rowIdx = idx + 1;
            int colIdx = 0;
            excelHelper.value(rowIdx, colIdx++, "");
            excelHelper.value(rowIdx, colIdx++, studentAudit.getId());
            // Add data column here
            // excelHelper.value(row.get(), col.getAndIncrement(), studentAudit.getXXX());
            excelHelper.value(rowIdx, colIdx++, studentAudit.getCreatedBy());
            excelHelper.value(rowIdx, colIdx++, studentAudit.getCreatedAt());
            excelHelper.value(rowIdx, colIdx++, studentAudit.getUpdatedBy());
            excelHelper.value(rowIdx, colIdx++, studentAudit.getUpdatedAt());
        });
        excelHelper.finish();

        FileResponse fileResponse = new FileResponse();
        fileResponse.setFilename(String.format("StudentAudits_%s.xlsx", parseDateTimeToString(LocalDateTime.now(), "yyyyMMdd(HH:mm:ss)")));
        fileResponse.setContentType(MediaType.valueOf("application/vnd.ms-excel"));
        fileResponse.setContentDisposition(ContentDisposition.parse(String.format("attachment; filename=%s", fileResponse.getFilename())));

        fileResponse.setResource(new ByteArrayResource(excelHelper.getBytes()));
        return AppResponse.okWithData(fileResponse);
    }
}