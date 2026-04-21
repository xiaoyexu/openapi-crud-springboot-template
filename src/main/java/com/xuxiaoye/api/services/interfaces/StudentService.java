package com.xuxiaoye.api.services.interfaces;

import com.xuxiaoye.api.adapter.api.server.dto.Student;
import com.xuxiaoye.api.adapter.api.server.dto.PagedStudents;
import com.xuxiaoye.api.adapter.api.server.dto.SearchStudentRequest;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.FileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface StudentService {
    AppResponse<PagedStudents> searchStudent(SearchStudentRequest searchStudentRequest, Pagination pagination);

    AppResponse<Student> getStudent(String id);

    AppResponse<Student> createStudent(Student student);

    AppResponse<Student> updateStudentById(String id, Student student);

    AppResponse<String> deleteStudentById(String id);

    AppResponse<FileResponse> exportStudents(SearchStudentRequest searchStudentRequest, Pagination pagination);

    AppResponse<String> importStudents(MultipartFile file);
}