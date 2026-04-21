package com.xuxiaoye.api.services.db;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xuxiaoye.api.services.db.dto.entity.StudentAudit;
import com.xuxiaoye.api.services.db.mapper.StudentAuditDBMapper;

public class StudentAuditDBService extends ServiceImpl<StudentAuditDBMapper, StudentAudit> {
    public List<StudentAudit> listAuditsByDataPkId(String studentId) {
        return this.list(new LambdaQueryWrapper<StudentAudit>().eq(StudentAudit::getId, studentId).orderByAsc(StudentAudit::getAuditId));
    }
}
