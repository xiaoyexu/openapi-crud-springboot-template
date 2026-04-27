package com.xuxiaoye.api.services.db.dto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("USERS")
@SuppressWarnings("java:S1068")
public class User extends BaseEntity{
    private String accountName;
    private String passwordHash;
    private String role;
    private String refreshToken;
}
