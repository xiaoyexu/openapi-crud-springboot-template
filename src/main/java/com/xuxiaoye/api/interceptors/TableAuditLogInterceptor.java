package com.xuxiaoye.api.interceptors;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.function.TriConsumer;
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

    @Getter
    public enum AuditAction {
        CREATE("A"), UPDATE("U"), DELETE("D");

        private final String action;

        AuditAction(String action) {
            this.action = action;
        }

        public static AuditAction fromAction(String action) {
            for (AuditAction auditAction : AuditAction.values()) {
                if (auditAction.action.equals(action)) {
                    return auditAction;
                }
            }
            throw new IllegalArgumentException("Unknow action: " + action);
        }
    }

    public final static String SUFFIX_AUDIT = "Audit";
    public final static String WRAPPER_KEY_ET = "et";
    public final static String WRAPPER_KEY_EW = "ew";

    private final Map<Class, TriConsumer<EntityInfo, AuditAction, BaseEntity>> ENTITY_INFO_MAP = Map.of(
            Student.class, (entityInfo, auditAction, baseEntity) -> saveAuditAsync(entityInfo.entityAuditDBMapper, this.getEntityMapper().map((Student) baseEntity), auditAction.action),
            Role.class, (entityInfo, auditAction, baseEntity) -> saveAuditAsync(entityInfo.entityAuditDBMapper, this.getEntityMapper().map((Role) baseEntity), auditAction.action),
            User.class, (entityInfo, auditAction, baseEntity) -> saveAuditAsync(entityInfo.entityAuditDBMapper, this.getEntityMapper().map((User) baseEntity), auditAction.action)
    );

    private final Map<String, EntityInfo> ENTITY_DB_MAP = Map.of(
            "StudentDBMapper", new EntityInfo(Student.class, StudentDBMapper.class, StudentAuditDBMapper.class),
            "RoleDBMapper", new EntityInfo(Role.class, RoleDBMapper.class, RoleAuditDBMapper.class),
            "UserDBMapper", new EntityInfo(User.class, UserDBMapper.class, UserAuditDBMapper.class)
    );

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
            saveEntityAudit(entityInfo, AuditAction.CREATE, (BaseEntity) parameter);
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
                saveEntityAudit(entityInfo, AuditAction.DELETE, entity);
                // Log "Create" for new Pk Id
                if (parameter instanceof Map) {
                    Object wrapper = ((Map<?, ?>) parameter).getOrDefault(WRAPPER_KEY_EW, null);
                    if (wrapper instanceof UpdateWrapper<?>) {
                        String paramName = extractNewId((UpdateWrapper<?>) wrapper);
                        String newId = (String) ((UpdateWrapper<?>) wrapper).getParamNameValuePairs().get(paramName);
                        BaseEntity newEntity = (BaseEntity) getEntityDBMapper(entityInfo.entityDBMapper).selectById(newId);
                        saveEntityAudit(entityInfo, AuditAction.CREATE, newEntity);
                    }

                    if (wrapper instanceof LambdaUpdateWrapper<?>) {
                        String paramName = extractNewId((LambdaUpdateWrapper<?>) wrapper);
                        String newId = (String) ((LambdaUpdateWrapper<?>) wrapper).getParamNameValuePairs().get(paramName);
                        BaseEntity newEntity = (BaseEntity) getEntityDBMapper(entityInfo.entityDBMapper).selectById(newId);
                        saveEntityAudit(entityInfo, AuditAction.CREATE, newEntity);
                    }
                }
            } else {
                if (parameter instanceof Map) {
                    Object etEntity = ((Map<?, ?>) parameter).getOrDefault(WRAPPER_KEY_ET, null);
                    Object wrapper = ((Map<?, ?>) parameter).getOrDefault(WRAPPER_KEY_EW, null);
                    if (etEntity instanceof BaseEntity) {
                        saveEntityAudit(entityInfo, AuditAction.UPDATE, (BaseEntity) etEntity);
                    }
                    if (wrapper instanceof UpdateWrapper<?> || wrapper instanceof LambdaUpdateWrapper<?>) {
                        saveEntityAudit(entityInfo, AuditAction.UPDATE, lastestEntity);
                    }
                }
            }
        });
    }

    protected void handleDelete(EntityInfo entityInfo, List<BaseEntity> deletedEntities) {
        deletedEntities.forEach(deleteEntity -> saveEntityAudit(entityInfo, AuditAction.DELETE, deleteEntity));
    }

    @Async
    protected int saveAuditAsync(Class<?> mapperClass, BaseAuditEntity auditEntity, String action) {
        auditEntity.setAction(action);
        return getEntityDBMapper(mapperClass).insert(auditEntity);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        TableAuditLogInterceptor.applicationContext = applicationContext;
    }

    private void saveEntityAudit(EntityInfo entityInfo, AuditAction auditAction, BaseEntity baseEntity) {
        ENTITY_INFO_MAP.get(entityInfo.entityClass).accept(entityInfo, auditAction, baseEntity);
    }

    EntityInfo identifyEntityClass(String msId) {
        String dbMapperName = "";

        int lastDot = msId.lastIndexOf('.');
        if (lastDot > 0) {
            int secondLastDot = msId.lastIndexOf('.', lastDot - 1);
            if (secondLastDot > 0) {
                dbMapperName = msId.substring(secondLastDot + 1, lastDot);
            }
        }
        return ENTITY_DB_MAP.get(dbMapperName);
    }
}
