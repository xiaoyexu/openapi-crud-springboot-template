package com.xuxiaoye.api.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import io.micrometer.common.util.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.services.db.dto.entity.BaseEntity;
import com.xuxiaoye.api.utils.DateTimeUtils;

@Log4j2
public class BaseDbClient {

    public enum Operator {
        IN,
        LIKE,
        I_LIKE,
        DATE_RANGE,
        DATETIME_RANGE,
        INTEGER_RANGE,
        DECIMAL_RANGE
    }

    protected <T, E> void addFilter(
            LambdaQueryWrapper<E> query,
            Operator operator,
            SFunction<E, ?> column,
            Collection<T> values
    ) {
        String columnName = LambdaUtils.extract(column).getImplMethodName().substring(3);
        if (!CollectionUtils.isEmpty(values)) {
            switch (operator) {
                case IN -> query.in(column, values);
                case LIKE -> query.and(subCondition -> values.forEach(
                        value -> subCondition.or(fieldCondition -> fieldCondition.like(column, value)))
                );
                case I_LIKE -> query.and(subCondition -> values.forEach(
                                value -> subCondition.or(
                                        fieldCondition -> fieldCondition.apply(
                                                "LOWER(" + columnName + ") LIKE CONCAT('%', {0}, '%')",
                                                ((String) value).toLowerCase()
                                        )
                                )
                        )
                );
                case DATE_RANGE -> query.and(subCondition ->
                        values.forEach(value -> subCondition.or(
                                fieldCondition -> handleRange(column, (String) value, fieldCondition, DateTimeUtils::parseStringToDate))
                        )
                );
                case DATETIME_RANGE -> query.and(subCondition -> values.forEach(
                                value -> subCondition.or(
                                        fieldCondition -> handleRange(column, (String) value, fieldCondition, DateTimeUtils::parseStringToDateTime)
                                )
                        )
                );
                case INTEGER_RANGE -> query.and(subCondition -> values.forEach(
                                value -> subCondition.or(
                                        fieldCondition -> handleRange(column, (String) value, fieldCondition, Integer::valueOf)
                                )
                        )
                );
                case DECIMAL_RANGE -> query.and(subCondition -> values.forEach(
                                value -> subCondition.or(
                                        fieldCondition -> handleRange(column, (String) value, fieldCondition, Double::valueOf)
                                )
                        )
                );
            }
        }
    }

    protected <E extends BaseEntity> void addSortField(
            LambdaQueryWrapper<E> query,
            Pagination pagination,
            Function<String, SFunction<E, ?>> columnMapper
    ) {
        if (ArrayUtils.isEmpty(pagination.getSortFields())) {
            query.orderByAsc(columnMapper.apply(""));
            return;
        }

        Arrays.stream(pagination.getSortFields()).forEach(sortField -> {
            SFunction<E, ?> column = columnMapper.apply(sortField.getFieldName());
            if (sortField.isAscending()) {
                query.orderByAsc(column);
            } else {
                query.orderByDesc(column);
            }
        });
    }

    protected static <T> void handleRange(
            SFunction<T, ?> column,
            String value,
            LambdaQueryWrapper<T> fieldCondition,
            Function<String, Object> converter
    ) {
        String[] values = value.split(",");
        if (values.length == 2 && !StringUtils.isBlank(values[0]) && !StringUtils.isBlank(values[1])) {
            fieldCondition.between(column, converter.apply(values[0]), converter.apply(values[1]));
        } else {
            if (values.length > 0 && !StringUtils.isBlank(values[0])) {
                fieldCondition.ge(column, converter.apply(values[0]));
            }
            if (values.length > 1 && !StringUtils.isBlank(values[1])) {
                fieldCondition.le(column, converter.apply(values[1]));
            }
        }
    }

    public <T> AppResponse<T> handleDbCall(Supplier<AppResponse<T>> logic) {
        try {
            return logic.get();
        } catch (MyBatisSystemException ex) {
            // Rollback
            rollback();
            log.error("db call error: {}", ex.getLocalizedMessage());
            AppStatus appStatus = AppStatus.builder().code("500").message(ex.getLocalizedMessage()).build();
            return AppResponse.failWithStatus(appStatus);
        } catch (RuntimeException e) {
            // Rollback
            rollback();
            AppStatus appStatus = AppStatus.builder().code("500").message(e.getLocalizedMessage()).build();
            return AppResponse.failWithStatus(appStatus);
        }
    }

    protected void rollback() {
        try {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (NoTransactionException e) {
            log.error("Rollback error: {}", e.getLocalizedMessage());
        }
    }
}
