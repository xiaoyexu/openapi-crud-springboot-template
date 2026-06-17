package com.xuxiaoye.api.services.interfaces;

import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.UserAuditMapper;
import com.xuxiaoye.api.services.db.UserAuditDBService;

public interface UserAuditService extends Service<String, UserAudit, SearchUserAuditRequest, PagedUserAudits, UserAuditMapper, UserAuditDBService> {
}