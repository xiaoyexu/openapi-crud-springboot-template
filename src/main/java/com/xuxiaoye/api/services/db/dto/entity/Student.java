package com.xuxiaoye.api.services.db.dto.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("STUDENTS")
@SuppressWarnings("java:S1068")
public class Student extends BaseEntity {
    private String name;
    private Integer age;
    private BigDecimal height;
    private LocalDate birthday;
}