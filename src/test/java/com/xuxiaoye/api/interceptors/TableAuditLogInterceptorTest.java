package com.xuxiaoye.api.interceptors;

import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.ActiveProfiles;

import com.xuxiaoye.api.Application;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
class TableAuditLogInterceptorTest {
    @InjectMocks
    TableAuditLogInterceptor tableAuditLogInterceptor;

    @Mock
    Invocation invocation;

    @Test
    void whenCannotIdentifyEntityClass() throws Throwable {
        invocation = mock(Invocation.class);
        Method method = mock(Method.class);

        when(invocation.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("update");

        MappedStatement ms = mock(MappedStatement.class);
        when(invocation.getArgs()).thenReturn(new Object[]{ms});
        when(ms.getId()).thenReturn("Invalid");

        when(invocation.proceed()).thenReturn(new Object());
        assertDoesNotThrow(() -> {
            tableAuditLogInterceptor.intercept(invocation);
        });
    }

//    @Test
//    void whenIdentifyAndAddEntity() throws Throwable {
//        invocation = mock(Invocation.class);
//        Method method = mock(Method.class);
//
//        when(invocation.getMethod()).thenReturn(method);
//        when(method.getName()).thenReturn("update");
//
//        MappedStatement ms = mock(MappedStatement.class);
//        when(invocation.getArgs()).thenReturn(new Object[]{ms});
//        when(ms.getId()).thenReturn("StudentDBMapper");
//        when(ms.getSqlCommandType()).thenReturn(SqlCommandType.UPDATE);
//
//        when(invocation.proceed()).thenReturn(new Object());
//        assertDoesNotThrow(() -> {
//            tableAuditLogInterceptor.intercept(invocation);
//        });
//    }
}