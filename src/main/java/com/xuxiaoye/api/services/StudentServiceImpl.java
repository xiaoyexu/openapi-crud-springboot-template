package com.xuxiaoye.api.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.dhatim.fastexcel.reader.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.xuxiaoye.api.adapter.api.server.dto.Student;
import com.xuxiaoye.api.adapter.api.server.dto.PagedStudents;
import com.xuxiaoye.api.adapter.api.server.dto.SearchStudentRequest;
import com.xuxiaoye.api.adapter.server.mapper.StudentMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.client.BaseDbClient;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.resp.FileResponse;
import com.xuxiaoye.api.services.db.StudentDBService;
import com.xuxiaoye.api.services.interfaces.StudentService;
import com.xuxiaoye.api.utils.ExcelHelper;

import static com.xuxiaoye.api.client.BaseDbClient.Operator.*;
import static com.xuxiaoye.api.constant.CommonConstants.*;
import static com.xuxiaoye.api.utils.DateTimeUtils.*;
import static com.xuxiaoye.api.interceptors.TableAuditLogInterceptor.*;

@Log4j2
public class StudentServiceImpl extends BaseDbClient implements StudentService {

    private final RequestContext requestContext;
    private final StudentMapper studentMapper;
    private final StudentDBService studentDBService;

    public StudentServiceImpl(
            RequestContext requestContext,
            StudentMapper studentMapper,
            StudentDBService studentDBService
    ) {
        this.requestContext = requestContext;
        this.studentMapper = studentMapper;
        this.studentDBService = studentDBService;
    }

    @Override
    public AppResponse<PagedStudents> listStudent(Pagination pagination) {
        return this.searchStudent(new SearchStudentRequest(), pagination);
    }

    @Override
    public AppResponse<PagedStudents> searchStudent(SearchStudentRequest searchStudentRequest, Pagination pagination) {
        return handleDbCall(() -> {
            Page<com.xuxiaoye.api.services.db.dto.entity.Student> page = new Page<>(pagination.getOffset(), pagination.getLimit());
            LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.Student> query = new LambdaQueryWrapper<>();

            addSortField(
                    query,
                    pagination,
                    sortField -> com.xuxiaoye.api.services.db.dto.entity.Student::getId
            );

            // Todo - Add search fields here
            addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.Student::getId, searchStudentRequest.getIds());
            addFilter(query, I_LIKE, com.xuxiaoye.api.services.db.dto.entity.Student::getName, searchStudentRequest.getNames());
            addFilter(query, INTEGER_RANGE, com.xuxiaoye.api.services.db.dto.entity.Student::getAge, searchStudentRequest.getAges());
            addFilter(query, DECIMAL_RANGE, com.xuxiaoye.api.services.db.dto.entity.Student::getHeight, searchStudentRequest.getHeights());
            addFilter(query, DATE_RANGE, com.xuxiaoye.api.services.db.dto.entity.Student::getBirthday, searchStudentRequest.getBirthdays());
            addFilter(query, DATETIME_RANGE, com.xuxiaoye.api.services.db.dto.entity.Student::getCreatedAt, searchStudentRequest.getCreatedAts());
            addFilter(query, DATETIME_RANGE, com.xuxiaoye.api.services.db.dto.entity.Student::getUpdatedAt, searchStudentRequest.getUpdatedAts());
            addFilter(query, LIKE, com.xuxiaoye.api.services.db.dto.entity.Student::getCreatedBy, searchStudentRequest.getCreatedBys());
            addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.Student::getUpdatedBy, searchStudentRequest.getUpdatedBys());

            // Keyword search
            if (StringUtils.isNotBlank(searchStudentRequest.getKeyword())) {
                applyMultiColumnKeyWordFilter(query, searchStudentRequest.getKeyword(),
                        com.xuxiaoye.api.services.db.dto.entity.Student::getName,
                        com.xuxiaoye.api.services.db.dto.entity.Student::getAge,
                        com.xuxiaoye.api.services.db.dto.entity.Student::getHeight,
                        com.xuxiaoye.api.services.db.dto.entity.Student::getBirthday
                );
            }

            Page<com.xuxiaoye.api.services.db.dto.entity.Student> studentPage = this.studentDBService.page(page, query);
            PagedStudents pagedStudents = new PagedStudents(page.getTotal(), this.studentMapper.map(studentPage.getRecords()));
            return AppResponse.okWithData(pagedStudents);
        });
    }

    @Override
    public AppResponse<Student> getStudent(String id) {
        return handleDbCall(() -> {
            com.xuxiaoye.api.services.db.dto.entity.Student student = this.studentDBService.getById(id);
            if (student == null) {
                return AppResponse.failWithStatus(AppStatus.notFound());
            }
            return AppResponse.okWithData(this.studentMapper.map(student));
        });
    }

    @Override
    @Transactional
    public AppResponse<Student> createStudent(Student student) {
        if (StringUtils.isBlank(student.getId())) {
            student.setId(UUID.randomUUID().toString());
        }

        AppResponse<Student> validateResult = validate(student);
        if (!validateResult.isOk()) {
            return validateResult;
        }

        return handleDbCall(() -> {
            com.xuxiaoye.api.services.db.dto.entity.Student dbStudent = this.studentMapper.map(student);
            dbStudent.setCreatedBy(requestContext.getXUserId());
            dbStudent.setCreatedAt(LocalDateTime.now());
            dbStudent.setUpdatedBy(requestContext.getXUserId());
            dbStudent.setUpdatedAt(LocalDateTime.now());
            if (this.studentDBService.save(dbStudent)) {
                return AppResponse.okWithData(this.studentMapper.map(dbStudent));
            } else {
                return AppResponse.failWithStatus(AppStatus.internalError());
            }
        });
    }

    @Override
    @Transactional
    public AppResponse<Student> updateStudentById(String id, Student student) {
        return handleDbCall(() -> {
            AppResponse<Student> validateResult = validate(student);
            if (!validateResult.isOk()) {
                return validateResult;
            }

            com.xuxiaoye.api.services.db.dto.entity.Student dbStudent = this.studentDBService.getById(id);
            if (dbStudent == null) {
                return AppResponse.failWithStatus(AppStatus.notFound());
            }

            student.setId(id);
            com.xuxiaoye.api.services.db.dto.entity.Student updatedDbStudent = this.studentMapper.map(student);
            updatedDbStudent.setUpdatedBy(requestContext.getXUserId());
            updatedDbStudent.setUpdatedAt(LocalDateTime.now());
            updatedDbStudent.setCreatedBy(dbStudent.getCreatedBy());
            updatedDbStudent.setCreatedAt(dbStudent.getCreatedAt());

            if (this.studentDBService.updateById(updatedDbStudent)) {
                return AppResponse.okWithData(this.studentMapper.map(updatedDbStudent));
            } else {
                return AppResponse.failWithStatus(AppStatus.internalError());
            }
        });
    }

    private AppResponse<Student> validate(Student student) {
        if (student.getAge() == null) {
            return AppResponse.failWithStatus(AppStatus.badRequest("Missing Age"));
        }
        return AppResponse.ok();
    }

    @Override
    @Transactional
    public AppResponse<String> deleteStudentById(String id) {
        return handleDbCall(() -> {
            if (this.studentDBService.getById(id) == null) {
                return AppResponse.failWithStatus(AppStatus.notFound());
            }

            if (this.studentDBService.removeById(id)) {
                return AppResponse.okWithData(OK);
            } else {
                return AppResponse.failWithStatus(AppStatus.internalError());
            }
        });
    }

    @Override
    public AppResponse<FileResponse> exportStudents(SearchStudentRequest searchStudentRequest, Pagination pagination) {
        AppResponse<PagedStudents> pagedStudentsAppResponse = this.searchStudent(searchStudentRequest, pagination);
        if (!pagedStudentsAppResponse.isOk()) {
            return AppResponse.failWithStatus(pagedStudentsAppResponse.getStatus());
        }

        ExcelHelper.ExcelWriter excelHelper = ExcelHelper.getWriter();
        excelHelper
                .newWorkbook("Student", "1.0")
                .newWorkSheet("Student");

        String[] headers = new String[]{
                "ACTION", // A - Add, U - Update , D - Delete
                "ID",
                "NAME",
                "AGE",
                // "XXX",
                "CREATED BY",
                "CREATED AT",
                "UPDATED BY",
                "UPDATED AT"
        };
        IntStream.range(0, headers.length).forEach(idx -> {
            excelHelper.value(0, idx, headers[idx]);
        });

        List<Student> students = pagedStudentsAppResponse.getData().getData();
        IntStream.range(0, students.size()).forEach(idx -> {
            Student student = students.get(idx);
            int rowIdx = idx + 1;
            int colIdx = 0;
            excelHelper.value(rowIdx, colIdx++, "");
            excelHelper.value(rowIdx, colIdx++, student.getId());
            excelHelper.value(rowIdx, colIdx++, student.getName());
            excelHelper.value(rowIdx, colIdx++, "" + student.getAge());
            excelHelper.value(rowIdx, colIdx++, student.getCreatedBy());
            excelHelper.value(rowIdx, colIdx++, student.getCreatedAt());
            excelHelper.value(rowIdx, colIdx++, student.getUpdatedBy());
            excelHelper.value(rowIdx, colIdx++, student.getUpdatedAt());
        });
        excelHelper.finish();

        FileResponse fileResponse = new FileResponse();
        fileResponse.setFilename(String.format("Students_%s.xlsx", parseDateTimeToString(LocalDateTime.now(), "yyyyMMdd(HH:mm:ss)")));
        fileResponse.setContentType(MediaType.valueOf("application/vnd.ms-excel"));
        fileResponse.setContentDisposition(ContentDisposition.parse(String.format("attachment; filename=%s", fileResponse.getFilename())));

        fileResponse.setResource(new ByteArrayResource(excelHelper.getBytes()));
        return AppResponse.okWithData(fileResponse);
    }

    @Override
    @Transactional
    public AppResponse<String> importStudents(MultipartFile file) {
        ReadableWorkbook readableWorkbook;
        try {
            readableWorkbook = new ReadableWorkbook(new ByteArrayInputStream(file.getBytes()));
        } catch (IOException e) {
            log.error("File process error: {}", e.getLocalizedMessage());
            return AppResponse.failWithStatus(AppStatus.internalError(e.getLocalizedMessage()));
        }

        return handleDbCall(() -> ExcelHelper.getReader(readableWorkbook).process(this::handleRow));
    }

    protected void handleRow(Row row) {
        int colIdx = 0;
        String action = (String) row.getCell(colIdx++).getValue();
        String id = (String) row.getCell(colIdx++).getValue();

        if (StringUtils.isBlank(action) || StringUtils.isBlank(id)) {
            return;
        }

        String name = (String) row.getCell(colIdx++).getValue();
        String ageValue = row.getCell(colIdx++).getRawValue();
        Integer age = Integer.parseInt(ageValue);

        // default columns
        String createdBy = (String) row.getCell(colIdx++).getValue();
        String createdAt = (String) row.getCell(colIdx++).getValue();
        String updatedBy = (String) row.getCell(colIdx++).getValue();
        String updatedAt = (String) row.getCell(colIdx++).getValue();

        Student student = Student.builder()
                .id(id)
                .name(name)
                .age(age)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
        switch (action) {
            case ACTION_CREATE -> this.createStudent(student);
            case ACTION_UPDATE -> this.updateStudentById(id, student);
            case ACTION_DELETE -> this.deleteStudentById(id);
        }
    }
}