package com.xuxiaoye.api.services.db.dto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dev.STUDENTS")
@SuppressWarnings("java:S1068")
public class Student extends BaseEntity {
    private String name;
    private Integer age;
}