package com.xuxiaoye.api.services.db.dto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ROLES_AUDIT")
@SuppressWarnings("java:S1068")
public class RoleAudit extends BaseAuditEntity {
    private String authority;
}
