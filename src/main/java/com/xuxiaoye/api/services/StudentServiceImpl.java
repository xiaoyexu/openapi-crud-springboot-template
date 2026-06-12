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

    // ========== Exception Handling & ifOk() Pattern Guidelines ==========
    //
    // Error Handling Strategy:
    // 1. Business validation (age != null) -> return AppResponse.failWithStatus()
    // 2. Database errors -> automatically wrapped by handleDbCall()
    // 3. AppException thrown during processing -> caught by BaseDbClient and converted to AppResponse
    //
    // Using ifOk() Functional Pattern:
    // Best used for chaining dependent operations:
    //   return this.search(request, pagination).ifOk(pagedEntity -> {
    //       // only execute if search succeeds, otherwise error propagates automatically
    //       return AppResponse.okWithData(transform(pagedEntity));
    //   });
    //
    // importData() and exportData() already use ifOk() pattern (inherited from CRUDDbClient)
    // The handleRow() method uses ifOkElse() for elegant CRUD operation chaining
    // ========================================================================

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
    protected Student buildFromRow(String id, int colIdx, Row row) {
        String name = (String) row.getCell(colIdx++).getValue();
        String ageValue = row.getCell(colIdx++).getRawValue();
        Integer age = Integer.parseInt(ageValue);

        // default columns
        String createdBy = row.getCellAsString(colIdx++).orElse(this.requestContext.getXUserId());
        String createdAt = row.getCellAsString(colIdx++).orElse(LocalDateTime.now().toString());
        String updatedBy = row.getCellAsString(colIdx++).orElse(this.requestContext.getXUserId());
        String updatedAt = row.getCellAsString(colIdx++).orElse(LocalDateTime.now().toString());

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