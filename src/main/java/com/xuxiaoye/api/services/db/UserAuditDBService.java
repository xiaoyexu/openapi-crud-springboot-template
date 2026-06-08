package com.xuxiaoye.api.services.db;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xuxiaoye.api.services.db.dto.entity.UserAudit;
import com.xuxiaoye.api.services.db.mapper.UserAuditDBMapper;

public class UserAuditDBService extends ServiceImpl<UserAuditDBMapper, UserAudit> {
    public List<UserAudit> listAuditsByDataPkId(String userId) {
        return this.list(new LambdaQueryWrapper<UserAudit>().eq(UserAudit::getId, userId).orderByAsc(UserAudit::getAuditId));
    }
}
