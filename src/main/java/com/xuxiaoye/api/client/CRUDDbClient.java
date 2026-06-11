package com.xuxiaoye.api.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.xuxiaoye.api.bean.PagedEntity;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.resp.FileResponse;
import com.xuxiaoye.api.services.db.dto.entity.DBEntity;
import com.xuxiaoye.api.services.interfaces.Service;
import com.xuxiaoye.api.utils.ExcelHelper;
import com.xuxiaoye.api.bean.RequestContext;

import static com.xuxiaoye.api.constant.CommonConstants.OK;
import static com.xuxiaoye.api.interceptors.TableAuditLogInterceptor.*;
import static com.xuxiaoye.api.utils.DateTimeUtils.parseDateTimeToString;

@Log4j2
public abstract class CRUDDbClient<
        PresentDto,
        SearchRequest,
        PresentPagedEntities,
        PresentMapper extends com.xuxiaoye.api.adapter.server.mapper.BaseMapper<PresentDto, PresentPagedEntities, Entity>,
        Entity extends DBEntity<String>,
        DBMapper extends BaseMapper<Entity>,
        DBService extends ServiceImpl<DBMapper, Entity>
        > extends BaseDbClient implements Service<PresentDto, SearchRequest, PresentPagedEntities, PresentMapper, DBService> {

    protected RequestContext requestContext;

    @Override
    public AppResponse<PresentDto> get(String id) {
        return handleDbCall(() -> {
            Entity entity = this.getDBService().getById(id);
            if (entity == null) {
                return AppResponse.failWithStatus(AppStatus.notFound());
            }
            return AppResponse.okWithData(this.getMapper().mapToPresent(entity));
        });
    }

    protected AppResponse<PresentDto> validate(PresentDto pEntity) {
        return AppResponse.ok();
    }

    @Override
    @Transactional
    public AppResponse<PresentDto> create(PresentDto pEntity) {
        Entity dbEntity = this.getMapper().mapToDB(pEntity);

        AppResponse<PresentDto> validateResult = validate(pEntity);
        if (!validateResult.isOk()) {
            return validateResult;
        }

        if (StringUtils.isBlank(dbEntity.getId())) {
            dbEntity.setId(UUID.randomUUID().toString());
        }

        return handleDbCall(() -> {
            dbEntity.setCreatedBy(this.requestContext.getXUserId());
            dbEntity.setCreatedAt(LocalDateTime.now());
            dbEntity.setUpdatedBy(this.requestContext.getXUserId());
            dbEntity.setUpdatedAt(LocalDateTime.now());
            if (this.getDBService().save(dbEntity)) {
                return AppResponse.okWithData(this.getMapper().mapToPresent(dbEntity));
            } else {
                return AppResponse.failWithStatus(AppStatus.internalError());
            }
        });
    }

    protected Entity populateFrom(Entity updatedEntity, Entity dbEntity) {
        return updatedEntity;
    }

    @Override
    @Transactional
    public AppResponse<PresentDto> updateById(String id, PresentDto pEntity) {
        return handleDbCall(() -> {
            AppResponse<PresentDto> validateResult = validate(pEntity);
            if (!validateResult.isOk()) {
                return validateResult;
            }

            Entity dbEntity = this.getDBService().getById(id);
            if (dbEntity == null) {
                return AppResponse.failWithStatus(AppStatus.notFound());
            }

            Entity updatedDbEntity = this.getMapper().mapToDB(pEntity);
            // In case need to set db value
            updatedDbEntity = populateFrom(updatedDbEntity, dbEntity);
            updatedDbEntity.setId(id);
            updatedDbEntity.setUpdatedBy(this.requestContext.getXUserId());
            updatedDbEntity.setUpdatedAt(LocalDateTime.now());
            updatedDbEntity.setCreatedBy(dbEntity.getCreatedBy());
            updatedDbEntity.setCreatedAt(dbEntity.getCreatedAt());

            if (this.getDBService().updateById(updatedDbEntity)) {
                return AppResponse.okWithData(this.getMapper().mapToPresent(updatedDbEntity));
            } else {
                return AppResponse.failWithStatus(AppStatus.internalError());
            }
        });
    }

    @Override
    @Transactional
    public AppResponse<String> deleteById(String id) {
        return handleDbCall(() -> {
            if (this.getDBService().getById(id) == null) {
                return AppResponse.failWithStatus(AppStatus.notFound());
            }

            if (this.getDBService().removeById(id)) {
                return AppResponse.okWithData(OK);
            } else {
                return AppResponse.failWithStatus(AppStatus.internalError());
            }
        });
    }

    protected LambdaQueryWrapper<Entity> buildQuery(
            LambdaQueryWrapper<Entity> query,
            SearchRequest searchRequest,
            Pagination pagination
    ) {
        return query;
    }

    public AppResponse<PagedEntity<PresentDto>> searchInternal(SearchRequest searchRequest, Pagination pagination) {
        return handleDbCall(() -> {
            Page<Entity> page = new Page<>(pagination.getOffset(), pagination.getLimit());
            LambdaQueryWrapper<Entity> query = new LambdaQueryWrapper<>();

            query = buildQuery(query, searchRequest, pagination);

            Page<Entity> entityPageResult = this.getDBService().page(page, query);
            PagedEntity<PresentDto> pagedEntities = new PagedEntity<>(page.getTotal(),
                    this.getMapper().mapListToPresent(entityPageResult.getRecords()));
            return AppResponse.okWithData(pagedEntities);
        });
    }

    @Override
    public AppResponse<PresentPagedEntities> search(SearchRequest searchRequest, Pagination pagination) {
        return handleDbCall(() -> {
            Page<Entity> page = new Page<>(pagination.getOffset(), pagination.getLimit());
            LambdaQueryWrapper<Entity> query = new LambdaQueryWrapper<>();

            query = buildQuery(query, searchRequest, pagination);

            Page<Entity> entityPageResult = this.getDBService().page(page, query);
            PagedEntity<PresentDto> pagedEntities = new PagedEntity<>(page.getTotal(),
                    this.getMapper().mapListToPresent(entityPageResult.getRecords()));
            return AppResponse.okWithData(this.getMapper().mapPagedToPresent(pagedEntities));
        });
    }

    @Transactional
    public AppResponse<String> importData(MultipartFile file) {
        ReadableWorkbook readableWorkbook;
        try {
            readableWorkbook = new ReadableWorkbook(new ByteArrayInputStream(file.getBytes()));
        } catch (IOException e) {
            log.error("File process error: {}", e.getLocalizedMessage());
            return AppResponse.failWithStatus(AppStatus.internalError(e.getLocalizedMessage()));
        }

        return handleDbCall(() -> ExcelHelper.getReader(readableWorkbook).process(this::handleRow));
    }

    protected PresentDto buildFromRow(String id, int colIdx, Row row) {
        return null;
    }

    protected AppResponse<String> handleRow(Row row) {
        int colIdx = 0;
        String action = (String) row.getCell(colIdx++).getValue();
        String id = (String) row.getCell(colIdx++).getValue();

        if (StringUtils.isBlank(action)) {
            return AppResponse.ok();
        }
        AuditAction auditAction = AuditAction.fromAction(action);
        if ((AuditAction.UPDATE == auditAction || AuditAction.DELETE == auditAction) && StringUtils.isBlank(id)) {
            return AppResponse.ok();
        }

        PresentDto pEntity = buildFromRow(id, colIdx, row);
        AppResponse<?> appResponse = switch (auditAction) {
            case CREATE -> this.create(pEntity);
            case UPDATE -> this.updateById(id, pEntity);
            case DELETE -> this.deleteById(id);
        };
        if (!appResponse.isOk()) {
            return AppResponse.failWithStatus(AppStatus.badRequest("Failed to import id " + id + " error:" + appResponse.getStatus().getMessage()));
        }
        return AppResponse.ok();
    }

    protected String[] getHeaders() {
        return new String[]{};
    }

    protected void writeRow(ExcelHelper.ExcelWriter excelWriter, int rowIdx, int colIdx, PresentDto presentDto) {

    }

    public AppResponse<FileResponse> exportData(SearchRequest searchRequest, Pagination pagination, String sheetName) {
        AppResponse<PagedEntity<PresentDto>> pagedFilesAppResponse = this.searchInternal(searchRequest, pagination);
        if (!pagedFilesAppResponse.isOk()) {
            return AppResponse.failWithStatus(pagedFilesAppResponse.getStatus());
        }

        ExcelHelper.ExcelWriter excelWriter = ExcelHelper.getWriter();
        excelWriter
                .newWorkbook(sheetName, "1.0")
                .newWorkSheet(sheetName);

        String[] headers = getHeaders();
        IntStream.range(0, headers.length).forEach(idx -> {
            excelWriter.value(0, idx, headers[idx]);
        });

        List<PresentDto> pagedEntities = pagedFilesAppResponse.getData().getData();
        IntStream.range(0, pagedEntities.size()).forEach(idx -> {
            PresentDto presentDto = pagedEntities.get(idx);
            writeRow(excelWriter, idx + 1, 0, presentDto);
        });

        excelWriter.finish();

        FileResponse fileResponse = new FileResponse();
        fileResponse.setFilename(String.format("%s_%s.xlsx", sheetName, parseDateTimeToString(LocalDateTime.now(), "yyyyMMdd(HH:mm:ss)")));
        fileResponse.setContentType(MediaType.valueOf("application/vnd.ms-excel"));
        fileResponse.setContentDisposition(ContentDisposition.parse(String.format("attachment; filename=%s", fileResponse.getFilename())));

        fileResponse.setResource(new ByteArrayResource(excelWriter.getBytes()));
        return AppResponse.okWithData(fileResponse);
    }
}
