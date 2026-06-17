package com.xuxiaoye.api.services.interfaces;

import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.RoleAuditMapper;
import com.xuxiaoye.api.services.db.RoleAuditDBService;

public interface RoleAuditService extends Service<String, RoleAudit, SearchRoleAuditRequest, PagedRoleAudits, RoleAuditMapper, RoleAuditDBService> {
}