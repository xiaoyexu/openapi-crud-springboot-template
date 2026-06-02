package com.xuxiaoye.api.services.db;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xuxiaoye.api.services.db.dto.entity.RoleAudit;
import com.xuxiaoye.api.services.db.mapper.RoleAuditDBMapper;

public class RoleAuditDBService extends ServiceImpl<RoleAuditDBMapper, RoleAudit> {
    public List<RoleAudit> listAuditsByDataPkId(String pkId) {
        return this.list(new LambdaQueryWrapper<RoleAudit>().eq(RoleAudit::getId, pkId).orderByAsc(RoleAudit::getAuditId));
    }
}