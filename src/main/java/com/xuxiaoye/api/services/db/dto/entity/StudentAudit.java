package com.xuxiaoye.api.services.db.dto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("STUDENTS_AUDIT")
@SuppressWarnings("java:S1068")
public class StudentAudit extends BaseAuditEntity{
    private String name;
    private Integer age;
}
