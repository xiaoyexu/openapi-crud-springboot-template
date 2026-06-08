package com.xuxiaoye.api.adapter.server.mapper;

import java.util.List;

import org.mapstruct.MappingConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.xuxiaoye.api.adapter.api.server.dto.PagedStudentAudits;
import com.xuxiaoye.api.adapter.api.server.dto.StudentAudit;
import com.xuxiaoye.api.bean.PagedEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StudentAuditMapper extends BaseMapper<StudentAudit, PagedStudentAudits, com.xuxiaoye.api.services.db.dto.entity.StudentAudit> {
    @Mapping(target = "auditId", source = "auditId")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "age", source = "age")
    @Mapping(target = "height", source = "height")
    @Mapping(target = "birthday", source = "birthday")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "updatedAt", source = "updatedAt")
    StudentAudit mapToPresent(com.xuxiaoye.api.services.db.dto.entity.StudentAudit studentAudit);

    @Mapping(target = "auditId", source = "auditId")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "age", source = "age")
    @Mapping(target = "height", source = "height")
    @Mapping(target = "birthday", source = "birthday")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "updatedAt", source = "updatedAt")
    com.xuxiaoye.api.services.db.dto.entity.StudentAudit mapToDB(StudentAudit studentAudit);

    List<StudentAudit> mapPagedToPresent(List<com.xuxiaoye.api.services.db.dto.entity.StudentAudit> studentAudits);

    @Mapping(target = "total", source = "total")
    @Mapping(target = "data", source = "data")
    PagedStudentAudits mapPagedToPresent(PagedEntity<StudentAudit> pagedEntities);
}