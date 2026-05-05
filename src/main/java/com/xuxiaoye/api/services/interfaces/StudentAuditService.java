package com.xuxiaoye.api.services.interfaces;

import com.xuxiaoye.api.adapter.api.server.dto.StudentAudit;
import com.xuxiaoye.api.adapter.api.server.dto.PagedStudentAudits;
import com.xuxiaoye.api.adapter.api.server.dto.SearchStudentAuditRequest;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.FileResponse;

public interface StudentAuditService {
    AppResponse<PagedStudentAudits> searchStudentAudit(SearchStudentAuditRequest searchStudentAuditRequest, Pagination pagination);

    AppResponse<StudentAudit> getStudentAudit(String id);

    AppResponse<FileResponse> exportStudentAudits(SearchStudentAuditRequest searchStudentAuditRequest, Pagination pagination);
}