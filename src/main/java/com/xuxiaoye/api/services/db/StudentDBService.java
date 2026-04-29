package com.xuxiaoye.api.services.db;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xuxiaoye.api.services.db.dto.entity.Student;
import com.xuxiaoye.api.services.db.mapper.StudentDBMapper;

public class StudentDBService extends ServiceImpl<StudentDBMapper, Student> {
    public boolean isOwner(String id, String username) {
        Student student = this.getById(id);
        return student != null && student.getCreatedBy().equals(username);
    }
}
