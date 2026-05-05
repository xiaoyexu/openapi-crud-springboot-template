package com.xuxiaoye.api.services.db.dto.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public abstract class BaseAuditEntity extends DBEntity<String> {
    @TableId(type = IdType.AUTO)
    Long auditId;
    String action;
}
