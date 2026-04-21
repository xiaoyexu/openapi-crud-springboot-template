package com.xuxiaoye.api.utils;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import lombok.extern.log4j.Log4j2;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;

@Log4j2
public class ExcelReaderHelper {
    private final ReadableWorkbook readableWorkbook;

    public ExcelReaderHelper(ReadableWorkbook readableWorkbook) {
        this.readableWorkbook = readableWorkbook;
    }

    public AppResponse<String> process(Consumer<Row> rowHandler) {
        Optional<Sheet> optionalSheet = this.readableWorkbook.getSheet(0);
        if (optionalSheet.isPresent()) {
            Sheet sheet = optionalSheet.get();
            try (Stream<Row> rows = sheet.openStream()) {
                rows
                        .filter(row -> row.getRowNum() != 1) // Remove first header line
                        .forEach(rowHandler);
            } catch (IOException e) {
                log.error("Failed to open xlsx file data, error: {}", e.getLocalizedMessage());
                return AppResponse.failWithStatus(AppStatus.badRequest(e.getLocalizedMessage()));
            }
        }
        return AppResponse.ok();
    }
}
