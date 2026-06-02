package com.xuxiaoye.api.services.db.dto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ROLES")
@SuppressWarnings("java:S1068")
public class Role extends BaseEntity {
    private String authority;
}