package com.xuxiaoye.api.interceptors;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
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

import com.xuxiaoye.api.services.db.dto.entity.*;
import com.xuxiaoye.api.services.db.dto.mapper.EntityMapper;
import com.xuxiaoye.api.services.db.mapper.*;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
})
@Log4j2
public class TableAuditLogInterceptor implements Interceptor, ApplicationContextAware {
    private static ApplicationContext applicationContext;
    private static final Pattern PATTERN = Pattern.compile("^#\\{.*\\.([a-zA-Z0-1]+)\\}$");

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
        Object result = null;

        if (sqlCommandType == SqlCommandType.INSERT) {
            result = invocation.proceed();
            handleInsert(entityIfo, parameter);
        } else if (sqlCommandType == SqlCommandType.UPDATE) {
            List<BaseEntity> entitiesBeforeChange = getEntitiesBeforeChange(entityIfo, parameter, ms);
            result = invocation.proceed();
            handleUpdate(entityIfo, entitiesBeforeChange, parameter);
        } else if (sqlCommandType == SqlCommandType.DELETE) {
            List<BaseEntity> entitiesBeforeDelete = getEntitiesBeforeChange(entityIfo, parameter, ms);
            result = invocation.proceed();
            handleDelete(entityIfo, entitiesBeforeDelete);
        }

        return result;
    }

    protected List<BaseEntity> getEntitiesBeforeChange(EntityInfo entityInfo, Object parameter, MappedStatement ms) {
        List<BaseEntity> auditEntities = new ArrayList<>();
        if (parameter instanceof String) {
            auditEntities.add((BaseEntity) getEntityDBMapper(entityInfo.entityDBMapper).selectById((String) parameter));
        } else if (parameter instanceof BaseEntity) {
            auditEntities.add((BaseEntity) parameter);
        } else if (parameter instanceof Map) {
            Object wrapper = ((Map<?, ?>) parameter).getOrDefault(WRAPPER_KEY_EW, null);
            Object entity = ((Map<?, ?>) parameter).getOrDefault(WRAPPER_KEY_ET, null);
            if (wrapper != null) {
                if (wrapper instanceof UpdateWrapper<?>) {
                    List<BaseEntity> entities = getEntityDBMapper(entityInfo.entityDBMapper).selectList((UpdateWrapper<?>) wrapper);
                    auditEntities.addAll(entities);
                }
                if (wrapper instanceof LambdaUpdateWrapper<?>) {
                    List<BaseEntity> entities = getEntityDBMapper(entityInfo.entityDBMapper).selectList((LambdaUpdateWrapper<?>) wrapper);
                    auditEntities.addAll(entities);
                }
            } else if (entity != null) {
                if (entity instanceof BaseEntity) {
                    BaseEntity entityBeforeChange = (BaseEntity) getEntityDBMapper(entityInfo.entityDBMapper).selectById(((BaseEntity) entity).getId());
                    auditEntities.add(entityBeforeChange);
                }
            }
        }

        return auditEntities;
    }

    private void handleInsert(EntityInfo entityInfo, Object parameter) {
        if (parameter instanceof BaseEntity) {
            saveEntityAudit(entityInfo, ACTION_CREATE, (BaseEntity) parameter);
        }
    }

    private String extractNewId(AbstractWrapper<?, ?, ?> wrapper) {
        return Arrays.stream(wrapper.getSqlSet().split(","))
                .map(set -> {
                    String[] setParts = set.split("=");
                    if (setParts[0].trim().equalsIgnoreCase("id")) {
                        Matcher matcher = PATTERN.matcher(setParts[1]);
                        if (matcher.find()) {
                            return matcher.group(1);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    protected void handleUpdate(EntityInfo entityInfo, List<BaseEntity> entitiesBeforeChange, Object parameter) {
        entitiesBeforeChange.stream().forEach(entity -> {
            BaseEntity lastestEntity = (BaseEntity) getEntityDBMapper(entityInfo.entityDBMapper).selectById(entity.getId());
            if (lastestEntity == null) {
                // PK Id changed
                // Log "Delete"
                saveEntityAudit(entityInfo, ACTION_DELETE, entity);
                // Log "Create" for new Pk Id
                if (parameter instanceof Map) {
                    Object wrapper = ((Map<?, ?>) parameter).getOrDefault(WRAPPER_KEY_EW, null);
                    if (wrapper instanceof UpdateWrapper<?>) {
                        String paramName = extractNewId((UpdateWrapper<?>) wrapper);
                        String newId = (String) ((UpdateWrapper<?>) wrapper).getParamNameValuePairs().get(paramName);
                        BaseEntity newEntity = (BaseEntity) getEntityDBMapper(entityInfo.entityDBMapper).selectById(newId);
                        saveEntityAudit(entityInfo, ACTION_CREATE, newEntity);
                    }

                    if (wrapper instanceof LambdaUpdateWrapper<?>) {
                        String paramName = extractNewId((LambdaUpdateWrapper<?>) wrapper);
                        String newId = (String) ((LambdaUpdateWrapper<?>) wrapper).getParamNameValuePairs().get(paramName);
                        BaseEntity newEntity = (BaseEntity) getEntityDBMapper(entityInfo.entityDBMapper).selectById(newId);
                        saveEntityAudit(entityInfo, ACTION_CREATE, newEntity);
                    }
                }
            } else {
                if (parameter instanceof Map) {
                    Object etEntity = ((Map<?, ?>) parameter).getOrDefault(WRAPPER_KEY_ET, null);
                    Object wrapper = ((Map<?, ?>) parameter).getOrDefault(WRAPPER_KEY_EW, null);
                    if (etEntity instanceof BaseEntity) {
                        saveEntityAudit(entityInfo, ACTION_UPDATE, (BaseEntity) etEntity);
                    }
                    if (wrapper instanceof UpdateWrapper<?> || wrapper instanceof LambdaUpdateWrapper<?>) {
                        saveEntityAudit(entityInfo, ACTION_UPDATE, lastestEntity);
                    }
                }
            }
        });
    }

    protected void handleDelete(EntityInfo entityInfo, List<BaseEntity> deletedEntities) {
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
        if (entityInfo.entityClass == Role.class) {
            RoleAudit audit = this.getEntityMapper().map((Role) baseEntity);
            audit.setAction(action);
            saveAuditAsync(entityInfo.entityAuditDBMapper, audit);
        }
        if (entityInfo.entityClass == User.class) {
            UserAudit audit = this.getEntityMapper().map((User) baseEntity);
            audit.setAction(action);
            saveAuditAsync(entityInfo.entityAuditDBMapper, audit);
        }
    }

    EntityInfo identifyEntityClass(String msId) {
        if (msId.contains("StudentDBMapper")) {
            return new EntityInfo(Student.class, StudentDBMapper.class, StudentAuditDBMapper.class);
        } else if (msId.contains("RoleDBMapper")) {
            return new EntityInfo(Role.class, RoleDBMapper.class, RoleAuditDBMapper.class);
        } else if (msId.contains("UserDBMapper")) {
            return new EntityInfo(User.class, UserDBMapper.class, UserAuditDBMapper.class);
        }
        return null;
    }

}
