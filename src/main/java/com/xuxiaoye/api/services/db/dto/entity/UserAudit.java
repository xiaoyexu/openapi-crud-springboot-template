package com.xuxiaoye.api.services.db.dto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("USERS_AUDIT")
@SuppressWarnings("java:S1068")
public class UserAudit extends BaseAuditEntity {
    private String accountName;
    private String passwordHash;
    private String role;
    private String refreshToken;
}
