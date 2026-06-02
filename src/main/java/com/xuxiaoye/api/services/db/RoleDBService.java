package com.xuxiaoye.api.services.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xuxiaoye.api.services.db.dto.entity.Role;
import com.xuxiaoye.api.services.db.mapper.RoleDBMapper;

public class RoleDBService extends ServiceImpl<RoleDBMapper, Role> {
    public boolean isOwner(String id, String createdBy) {
        Role role = this.getById(id);
        return role != null && role.getCreatedBy().equals(createdBy);
    }
}