package com.xuxiaoye.api.utils;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Log4j2
public class ExcelHelper {
    private ByteArrayOutputStream os;
    private Workbook wb;
    private Worksheet ws;

    @Getter
    private boolean isSuccess = false;

    public ExcelHelper() {
        this.os = new ByteArrayOutputStream();
    }

    public ExcelHelper newWorkbook(String applicationName, String version) {
        this.wb = new Workbook(os, applicationName, version);
        this.isSuccess = true;
        return this;
    }

    public ExcelHelper newWorkSheet(String name) {
        this.ws = this.wb.newWorksheet(name);
        this.isSuccess = true;
        return this;
    }

    public ExcelHelper value(int row, int col, String value) {
        this.ws.value(row, col, value);
        this.isSuccess = true;
        return this;
    }

    public ExcelHelper finish() {
        try {
            wb.finish();
            this.isSuccess = true;
            return this;
        } catch (IOException e) {
            log.error("Save work sheet error: {}", e.getLocalizedMessage());
            this.isSuccess = false;
            return this;
        }
    }

    public byte[] getBytes() {
        return os.toByteArray();
    }
}
