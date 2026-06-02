package com.xuxiaoye.api.services.interfaces;

import com.xuxiaoye.api.adapter.api.server.dto.Student;
import com.xuxiaoye.api.adapter.api.server.dto.PagedStudents;
import com.xuxiaoye.api.adapter.api.server.dto.SearchStudentRequest;
import com.xuxiaoye.api.adapter.server.mapper.StudentMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.FileResponse;
import com.xuxiaoye.api.services.db.StudentDBService;

public interface StudentService extends Service<Student, SearchStudentRequest, PagedStudents, StudentMapper, StudentDBService> {
    AppResponse<PagedStudents> listStudent(Pagination pagination);

    AppResponse<FileResponse> exportStudents(SearchStudentRequest searchStudentRequest, Pagination pagination);
}