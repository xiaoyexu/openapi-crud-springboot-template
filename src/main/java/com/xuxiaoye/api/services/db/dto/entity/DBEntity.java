package com.xuxiaoye.api.services.db.dto.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public abstract class DBEntity<T> {
    protected T id;
    protected String createdBy;
    protected LocalDateTime createdAt;
    protected String updatedBy;
    protected LocalDateTime updatedAt;
}
