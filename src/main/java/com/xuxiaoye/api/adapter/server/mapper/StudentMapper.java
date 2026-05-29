package com.xuxiaoye.api.adapter.server.mapper;

import java.util.List;

import org.mapstruct.MappingConstants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.xuxiaoye.api.adapter.api.server.dto.Student;
import com.xuxiaoye.api.adapter.api.server.dto.PagedStudents;
import com.xuxiaoye.api.bean.PagedEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StudentMapper extends BaseMapper<Student, PagedStudents, com.xuxiaoye.api.services.db.dto.entity.Student> {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "age", source = "age")
    @Mapping(target = "height", source = "height")
    @Mapping(target = "birthday", source = "birthday")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "updatedAt", source = "updatedAt")
    Student mapToPresent(com.xuxiaoye.api.services.db.dto.entity.Student student);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "age", source = "age")
    @Mapping(target = "height", source = "height")
    @Mapping(target = "birthday", source = "birthday")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "updatedAt", source = "updatedAt")
    com.xuxiaoye.api.services.db.dto.entity.Student mapToDB(Student student);

    List<Student> mapListToPresent(List<com.xuxiaoye.api.services.db.dto.entity.Student> students);

    @Mapping(target = "total", source = "total")
    @Mapping(target = "data", source = "data")
    PagedStudents mapPagedToPresent(PagedEntity<Student> pagedEntities);
}