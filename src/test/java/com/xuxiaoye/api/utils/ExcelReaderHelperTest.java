package com.xuxiaoye.api.utils;

import java.io.IOException;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Sheet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.resp.AppResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
class ExcelReaderHelperTest {
    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    void processXLSXEmptyFileError() throws IOException {
        ReadableWorkbook readableWorkbook = mock(ReadableWorkbook.class);
        Sheet sheet = mock(Sheet.class);
        when(readableWorkbook.getSheet(eq(0))).thenReturn(Optional.of(sheet));
        when(sheet.openStream()).thenThrow(new IOException("test error"));

        AppResponse<String> response = new ExcelReaderHelper(readableWorkbook).process(row -> {
        });
        assertThat(response.isOk()).isFalse();
    }

    @Test
    void processXLSXEmptySheetError() {
        ReadableWorkbook readableWorkbook = mock(ReadableWorkbook.class);
        when(readableWorkbook.getSheet(eq(0))).thenReturn(Optional.empty());

        AppResponse<String> response = new ExcelReaderHelper(readableWorkbook).process(row -> {
        });
        assertThat(response.isOk()).isTrue();
    }
}