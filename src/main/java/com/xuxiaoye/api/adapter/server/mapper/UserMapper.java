package com.xuxiaoye.api.adapter.server.mapper;

import java.util.List;

import org.mapstruct.MappingConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.xuxiaoye.api.adapter.api.server.dto.User;
import com.xuxiaoye.api.adapter.api.server.dto.PagedUsers;
import com.xuxiaoye.api.bean.PagedEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper extends BaseMapper<User, PagedUsers, com.xuxiaoye.api.services.db.dto.entity.User> {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "accountName", source = "accountName")
//    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "role", source = "role")
//    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "updatedAt", source = "updatedAt")
    User mapToPresent(com.xuxiaoye.api.services.db.dto.entity.User user);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "accountName", source = "accountName")
//    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "role", source = "role")
//    @Mapping(target = "refreshToken", source = "refreshToken")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "updatedAt", source = "updatedAt")
    com.xuxiaoye.api.services.db.dto.entity.User mapToDB(User user);

    List<User> mapListToPresent(List<com.xuxiaoye.api.services.db.dto.entity.User> users);

    @Mapping(target = "total", source = "total")
    @Mapping(target = "data", source = "data")
    PagedUsers mapPagedToPresent(PagedEntity<User> pagedEntities);
}