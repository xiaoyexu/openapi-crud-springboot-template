package com.xuxiaoye.api.services;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.dhatim.fastexcel.reader.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;

import com.xuxiaoye.api.adapter.api.server.dto.Student;
import com.xuxiaoye.api.adapter.api.server.dto.PagedStudents;
import com.xuxiaoye.api.adapter.api.server.dto.SearchStudentRequest;
import com.xuxiaoye.api.adapter.server.mapper.StudentMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.client.CRUDDbClient;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.resp.FileResponse;
import com.xuxiaoye.api.services.db.mapper.StudentDBMapper;
import com.xuxiaoye.api.services.db.StudentDBService;
import com.xuxiaoye.api.services.interfaces.StudentService;
import com.xuxiaoye.api.utils.ExcelHelper;

import static com.xuxiaoye.api.client.BaseDbClient.Operator.*;
import static com.xuxiaoye.api.utils.DateTimeUtils.*;

@Log4j2
public class StudentServiceImpl extends CRUDDbClient<
        Student,
        SearchStudentRequest,
        PagedStudents,
        StudentMapper,
        com.xuxiaoye.api.services.db.dto.entity.Student,
        StudentDBMapper,
        StudentDBService
        > implements StudentService {

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
    public StudentMapper getMapper() {
        return this.studentMapper;
    }

    @Override
    public StudentDBService getDBService() {
        return this.studentDBService;
    }

    @Override
    public AppResponse<PagedStudents> listStudent(Pagination pagination) {
        return this.search(new SearchStudentRequest(), pagination);
    }

    @Override
    public LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.Student> buildQuery(
            LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.Student> query,
            SearchStudentRequest searchStudentRequest, Pagination pagination) {

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
        return query;
    }


    @Override
    protected AppResponse<Student> validate(Student student) {
        if (student.getAge() == null) {
            return AppResponse.failWithStatus(AppStatus.badRequest("Missing Age"));
        }
        return AppResponse.ok();
    }


    @Override
    public AppResponse<FileResponse> exportStudents(SearchStudentRequest searchStudentRequest, Pagination pagination) {
        AppResponse<PagedStudents> pagedStudentsAppResponse = this.search(searchStudentRequest, pagination);
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
    protected Student buildFromRow(String id, int colIdx, Row row) {
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
        return student;
    }
}