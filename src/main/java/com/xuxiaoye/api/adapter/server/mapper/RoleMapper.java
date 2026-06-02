package com.xuxiaoye.api.adapter.server.mapper;

import com.xuxiaoye.api.adapter.api.server.dto.PagedRoles;
import com.xuxiaoye.api.adapter.api.server.dto.Role;
import com.xuxiaoye.api.bean.PagedEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleMapper extends BaseMapper<Role, PagedRoles, com.xuxiaoye.api.services.db.dto.entity.Role> {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "authority", source = "authority")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "updatedAt", source = "updatedAt")
    Role mapToPresent(com.xuxiaoye.api.services.db.dto.entity.Role role);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "authority", source = "authority")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "updatedAt", source = "updatedAt")
    com.xuxiaoye.api.services.db.dto.entity.Role mapToDB(Role role);

    List<Role> mapListToPresent(List<com.xuxiaoye.api.services.db.dto.entity.Role> roles);

    @Mapping(target = "total", source = "total")
    @Mapping(target = "data", source = "data")
    PagedRoles mapPagedToPresent(PagedEntity<Role> pagedEntities);
}