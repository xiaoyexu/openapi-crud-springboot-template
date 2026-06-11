package com.xuxiaoye.api.adapter.server.mapper;

import java.util.List;

import org.mapstruct.MappingConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.bean.PagedEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserAuditMapper extends BaseMapper<UserAudit, PagedUserAudits, com.xuxiaoye.api.services.db.dto.entity.UserAudit> {
    @Mapping(target = "auditId", source = "auditId")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "accountName", source = "accountName")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "updatedAt", source = "updatedAt")
    UserAudit mapToPresent(com.xuxiaoye.api.services.db.dto.entity.UserAudit userAudit);

    @Mapping(target = "auditId", source = "auditId")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "accountName", source = "accountName")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "updatedAt", source = "updatedAt")
    com.xuxiaoye.api.services.db.dto.entity.UserAudit mapToDB(UserAudit userAudit);

    List<UserAudit> mapListToPresent(List<com.xuxiaoye.api.services.db.dto.entity.UserAudit> userAudits);

    @Mapping(target = "total", source = "total")
    @Mapping(target = "data", source = "data")
    PagedUserAudits mapPagedToPresent(PagedEntity<UserAudit> pagedEntities);
}