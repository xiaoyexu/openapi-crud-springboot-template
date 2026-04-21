package com.xuxiaoye.api.interceptors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;

import com.xuxiaoye.api.services.db.dto.entity.*;
import com.xuxiaoye.api.services.db.dto.mapper.EntityMapper;
import com.xuxiaoye.api.services.db.mapper.*;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
})
@Log4j2
public class TableAuditLogInterceptor implements Interceptor, ApplicationContextAware {
    private static ApplicationContext applicationContext;

    protected BaseMapper getEntityDBMapper(Class clazz) {
        return (BaseMapper) applicationContext.getBean(clazz);
    }

    protected EntityMapper getEntityMapper() {
        return applicationContext.getBean(EntityMapper.class);
    }

    record EntityInfo(Class<?> entityClass, Class<?> entityDBMapper, Class<?> entityAuditDBMapper) {
    }

    public final static String ACTION_CREATE = "A";
    public final static String ACTION_UPDATE = "U";
    public final static String ACTION_DELETE = "D";
    public final static String SUFFIX_AUDIT = "Audit";
    public final static String WRAPPER_KEY_ET = "et";
    public final static String WRAPPER_KEY_EW = "ew";

    @Override

    public Object intercept(Invocation invocation) throws Throwable {
        log.debug("AuditLogInterceptor: Intercepting method - " + invocation.getMethod().getName());

        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        SqlCommandType sqlCommandType = ms.getSqlCommandType();

        String methodName = ms.getId();
        if (methodName.contains(SUFFIX_AUDIT)) {
            return invocation.proceed();
        }

        EntityInfo entityIfo = identifyEntityClass(ms.getId());
        if (entityIfo == null) {
            // Not able to identify table
            return invocation.proceed();
        }

        Object parameter = invocation.getArgs()[1];

        List<BaseEntity> auditEntities = new ArrayList<>();

        if (sqlCommandType == SqlCommandType.DELETE || sqlCommandType == SqlCommandType.UPDATE) {
            auditEntities = getAuditEntities(entityIfo, parameter, ms);
        }

        Object result = invocation.proceed();

        if (sqlCommandType == SqlCommandType.INSERT) {
            handleInsert(entityIfo, parameter);
        } else if (sqlCommandType == SqlCommandType.UPDATE) {
            handleUpdate(entityIfo, auditEntities, parameter);
        } else if (sqlCommandType == SqlCommandType.DELETE) {
            handleDelete(entityIfo, auditEntities);
        }

        return result;
    }

    protected List<BaseEntity> getAuditEntities(EntityInfo entityInfo, Object parameter, MappedStatement ms) {
        List<BaseEntity> auditEntities = new ArrayList<>();
        identifyAndAddEntity(entityInfo, parameter, auditEntities);
        return auditEntities;
    }

    protected void identifyAndAddEntity(EntityInfo entityInfo, Object parameter, List<BaseEntity> auditEntities) {
        if (parameter instanceof String) {
            auditEntities.add((BaseEntity) getEntityDBMapper(entityInfo.entityDBMapper).selectById((String) parameter));
        } else if (parameter instanceof BaseEntity) {
            auditEntities.add((BaseEntity) parameter);
        } else if (parameter instanceof Map) {
            Object wrapper = ((Map<?, ?>) parameter).getOrDefault(WRAPPER_KEY_EW, null);
            if (wrapper instanceof UpdateWrapper<?>) {
                List<BaseEntity> entities = getEntityDBMapper(entityInfo.entityDBMapper).selectList((UpdateWrapper) wrapper);
                auditEntities.addAll(entities);
            }
            if (wrapper instanceof LambdaUpdateWrapper<?>) {
                List<BaseEntity> entities = getEntityDBMapper(entityInfo.entityDBMapper).selectList((LambdaUpdateWrapper) wrapper);
                auditEntities.addAll(entities);
            }
        }
    }

    private void handleInsert(EntityInfo entityInfo, Object parameter) {
        if (parameter instanceof BaseEntity) {
            saveEntityAudit(entityInfo, ACTION_CREATE, (BaseEntity) parameter);
        }
    }

    private void handleUpdate(EntityInfo entityInfo, List<BaseEntity> updatedEntities, Object parameter) {
        List<String> ids = updatedEntities.stream().map(BaseEntity::getId).toList();
        if (!CollectionUtils.isEmpty(updatedEntities)) {
            List<BaseEntity> reloadEntities = getEntityDBMapper(entityInfo.entityDBMapper)
                    .selectByIds(ids);
            if (CollectionUtils.isEmpty(reloadEntities)) {
                // old record can't be found, id changed
                // todo - how to log new record with new id
                updatedEntities.forEach(updatedEntity -> saveEntityAudit(entityInfo, ACTION_DELETE, updatedEntity));
            } else {
                reloadEntities.forEach(updatedEntity -> saveEntityAudit(entityInfo, ACTION_UPDATE, updatedEntity));
            }
        }

        if (parameter instanceof Map) {
            handleUpdateByWrapper(entityInfo, (Map<?, ?>) parameter);
            return;
        }
        if (parameter instanceof BaseEntity) {
            saveEntityAudit(entityInfo, ACTION_UPDATE, (BaseEntity) parameter);
        }
    }

    private void handleUpdateByWrapper(EntityInfo entityInfo, Map<?, ?> parameter) {
        Object entity = parameter.getOrDefault(WRAPPER_KEY_ET, null);
        Object wrapper = parameter.getOrDefault(WRAPPER_KEY_EW, null);
        if (entity instanceof BaseEntity) {
            saveEntityAudit(entityInfo, ACTION_UPDATE, (BaseEntity) entity);
        }
    }

    private void handleDelete(EntityInfo entityInfo, List<BaseEntity> deletedEntities) {
        deletedEntities.forEach(deleteEntity -> saveEntityAudit(entityInfo, ACTION_DELETE, deleteEntity));
    }

    @Async
    protected int saveAuditAsync(Class<?> mapperClass, BaseAuditEntity auditEntity) {
        return getEntityDBMapper(mapperClass).insert(auditEntity);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        TableAuditLogInterceptor.applicationContext = applicationContext;
    }

    private void saveEntityAudit(EntityInfo entityInfo, String action, BaseEntity baseEntity) {
        if (entityInfo.entityClass == Student.class) {
            StudentAudit audit = this.getEntityMapper().map((Student) baseEntity);
            audit.setAction(action);
            saveAuditAsync(entityInfo.entityAuditDBMapper, audit);
        }
    }

    EntityInfo identifyEntityClass(String msId) {
        if (msId.contains("StudentDBMapper")) {
            return new EntityInfo(Student.class, StudentDBMapper.class, StudentAuditDBMapper.class);
        }
        return null;
    }

}
