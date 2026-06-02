package com.xuxiaoye.api.adapter.server.mapper;

import com.xuxiaoye.api.adapter.api.server.dto.PagedRoleAudits;
import com.xuxiaoye.api.adapter.api.server.dto.RoleAudit;
import com.xuxiaoye.api.bean.PagedEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleAuditMapper extends BaseMapper<RoleAudit, PagedRoleAudits, com.xuxiaoye.api.services.db.dto.entity.RoleAudit> {
    @Mapping(target = "auditId", source = "auditId")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "authority", source = "authority")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "updatedAt", source = "updatedAt")
    RoleAudit mapToPresent(com.xuxiaoye.api.services.db.dto.entity.RoleAudit roleAudit);

    @Mapping(target = "auditId", source = "auditId")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "authority", source = "authority")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "updatedAt", source = "updatedAt")
    com.xuxiaoye.api.services.db.dto.entity.RoleAudit mapToDB(RoleAudit roleAudit);

    List<RoleAudit> mapListToPresent(List<com.xuxiaoye.api.services.db.dto.entity.RoleAudit> roleAudits);

    @Mapping(target = "total", source = "total")
    @Mapping(target = "data", source = "data")
    PagedRoleAudits mapPagedToPresent(PagedEntity<RoleAudit> pagedEntities);
}