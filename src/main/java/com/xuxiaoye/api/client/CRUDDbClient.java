package com.xuxiaoye.api.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.xuxiaoye.api.bean.PagedEntity;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.services.db.dto.entity.BaseEntity;
import com.xuxiaoye.api.services.interfaces.Service;
import com.xuxiaoye.api.utils.ExcelHelper;
import com.xuxiaoye.api.bean.RequestContext;

import static com.xuxiaoye.api.constant.CommonConstants.OK;
import static com.xuxiaoye.api.interceptors.TableAuditLogInterceptor.*;

@Log4j2
public abstract class CRUDDbClient<
        PresentDto,
        PresentPagedEntities,
        Entity extends BaseEntity,
        PresentMapper extends com.xuxiaoye.api.adapter.server.mapper.BaseMapper<PresentDto, PresentPagedEntities, Entity>,
        DBMapper extends BaseMapper<Entity>,
        DBService extends ServiceImpl<DBMapper, Entity>,
        SearchRequest
        > extends BaseDbClient implements Service<PresentDto, PresentMapper, DBService, SearchRequest, PresentPagedEntities> {

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

        if (StringUtils.isBlank(action) || StringUtils.isBlank(id)) {
            return AppResponse.ok();
        }

        PresentDto pEntity = buildFromRow(id, colIdx, row);
        AppResponse<?> appResponse = switch (action) {
            case ACTION_CREATE -> this.create(pEntity);
            case ACTION_UPDATE -> this.updateById(id, pEntity);
            case ACTION_DELETE -> this.deleteById(id);
            default -> AppResponse.ok();
        };
        if (!appResponse.isOk()) {
            return AppResponse.failWithStatus(AppStatus.badRequest("Failed to import id " + id + " error:" + appResponse.getStatus().getMessage()));
        }
        return AppResponse.ok();
    }
}
