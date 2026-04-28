package com.xuxiaoye.api.adapter.server;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.api.server.StudentsApiDelegate;
import com.xuxiaoye.api.adapter.server.mapper.CommonMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.resp.FileResponse;
import com.xuxiaoye.api.services.interfaces.StudentService;

public class StudentAdapter implements StudentsApiDelegate {

    private final CommonMapper commonMapper;
    private final StudentService studentService;

    public StudentAdapter(
            CommonMapper commonMapper,
            StudentService studentService
    ) {
        this.commonMapper = commonMapper;
        this.studentService = studentService;
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'student', 'create')")
    public ResponseEntity<CreateStudentResponse> createSingleStudent(
            Student createStudentRequest
    ) {
        return this.studentService.createStudent(createStudentRequest)
                .toResponseEntity(
                        data -> CreateStudentResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> CreateStudentResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, #studentId, 'student', 'delete') or @P.hasPermission(authentication, #studentId, 'student', 'delete_own')")
    public ResponseEntity<DeleteStudentResponse> deleteSingleStudent(
            String studentId
    ) {
        return this.studentService.deleteStudentById(studentId)
                .toResponseEntity(
                        data -> DeleteStudentResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> DeleteStudentResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, #studentId, 'student', 'get') or @P.hasPermission(authentication, #studentId, 'student', 'get_own')")
    public ResponseEntity<GetStudentResponse> getSingleStudent(
            String studentId
    ) {
        return this.studentService.getStudent(studentId)
                .toResponseEntity(
                        data -> GetStudentResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> GetStudentResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or  @P.hasPermission(authentication, 'student', 'search')")
    public ResponseEntity<SearchStudentResponse> searchStudents(
            SearchStudentRequest searchStudentRequest,
            Integer limit,
            Integer offset,
            String sortBy
    ) {
        return this.studentService.searchStudent(searchStudentRequest, Pagination.of(offset, limit, sortBy))
                .toResponseEntity(
                        data -> SearchStudentResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> SearchStudentResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, #studentId, 'student', 'update') or @P.hasPermission(authentication, #studentId, 'student', 'update_own')")
    public ResponseEntity<UpdateStudentResponse> updateSingleStudent(
            String studentId,
            Student updateStudentRequest
    ) {
        return this.studentService.updateStudentById(studentId, updateStudentRequest)
                .toResponseEntity(
                        data -> UpdateStudentResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> UpdateStudentResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'student', 'export')")
    public ResponseEntity<org.springframework.core.io.Resource> exportStudents(
            SearchStudentRequest searchStudentRequest,
            Integer limit,
            Integer offset,
            String sortBy
    ) {
        AppResponse<FileResponse> resourceAppResponse = this.studentService.exportStudents(searchStudentRequest, Pagination.of(offset, limit, sortBy));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(resourceAppResponse.getData().getContentType());
        headers.setContentDisposition(resourceAppResponse.getData().getContentDisposition());
        return new ResponseEntity<>(resourceAppResponse.getData().getResource(), headers, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'student', 'import')")
    public ResponseEntity<ImportStudentResponse> importStudents(MultipartFile file) {
        return this.studentService.importStudents(file)
                .toResponseEntity(
                        data -> ImportStudentResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> ImportStudentResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }
}