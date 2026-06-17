package com.xuxiaoye.api.services.interfaces;

import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.RoleMapper;
import com.xuxiaoye.api.services.db.RoleDBService;

public interface RoleService extends Service<String, Role, SearchRoleRequest, PagedRoles, RoleMapper, RoleDBService> {
}