package com.xuxiaoye.api.client;

import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;

@Slf4j
public class BaseDbClient {

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
