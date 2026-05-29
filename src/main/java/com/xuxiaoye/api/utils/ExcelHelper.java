package com.xuxiaoye.api.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;

@Log4j2
public class ExcelHelper {
    private ExcelHelper() {
    }

    public static class ExcelReader {
        private final ReadableWorkbook readableWorkbook;

        public ExcelReader(ReadableWorkbook readableWorkbook) {
            this.readableWorkbook = readableWorkbook;
        }

        public AppResponse<String> process(Function<Row, AppResponse<String>> rowHandler) {
            Optional<Sheet> optionalSheet = this.readableWorkbook.getSheet(0);
            if (optionalSheet.isPresent()) {
                Sheet sheet = optionalSheet.get();
                try (Stream<Row> rows = sheet.openStream()) {
                    List<AppResponse<String>> result = rows
                            .filter(row -> row.getRowNum() != 1) // Remove first header line
                            .map(rowHandler).toList();

                    Optional<AppResponse<String>> error = result.stream().filter(stringAppResponse -> !stringAppResponse.isOk()).findFirst();
                    return error.<AppResponse<String>>map(
                                    stringAppResponse -> AppResponse.failWithStatus(AppStatus.badRequest(stringAppResponse.getStatus().getMessage()))
                            )
                            .orElseGet(AppResponse::ok);
                } catch (IOException e) {
                    log.error("Failed to open xlsx file data, error: {}", e.getLocalizedMessage());
                    return AppResponse.failWithStatus(AppStatus.badRequest(e.getLocalizedMessage()));
                }
            }
            return AppResponse.ok();
        }
    }

    public static class ExcelWriter {
        private ByteArrayOutputStream os;
        private Workbook wb;
        private Worksheet ws;

        @Getter
        private boolean isSuccess = false;

        public ExcelWriter() {
            this.os = new ByteArrayOutputStream();
        }

        public ExcelWriter newWorkbook(String applicationName, String version) {
            this.wb = new Workbook(os, applicationName, version);
            this.isSuccess = true;
            return this;
        }

        public ExcelWriter newWorkbook(Workbook workbook) {
            this.wb = workbook;
            this.isSuccess = true;
            return this;
        }

        public ExcelWriter newWorkSheet(String name) {
            this.ws = this.wb.newWorksheet(name);
            this.isSuccess = true;
            return this;
        }

        public ExcelWriter value(int row, int col, String value) {
            this.ws.value(row, col, value);
            this.isSuccess = true;
            return this;
        }

        public ExcelWriter finish() {
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

    public static ExcelWriter getWriter() {
        return new ExcelWriter();
    }

    public static ExcelReader getReader(ReadableWorkbook readableWorkbook) {
        return new ExcelReader(readableWorkbook);
    }
}
