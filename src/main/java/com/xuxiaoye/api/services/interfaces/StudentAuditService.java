package com.xuxiaoye.api.services.interfaces;

import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.StudentAuditMapper;
import com.xuxiaoye.api.services.db.StudentAuditDBService;

public interface StudentAuditService extends Service<StudentAudit, SearchStudentAuditRequest, PagedStudentAudits, StudentAuditMapper, StudentAuditDBService>{
}