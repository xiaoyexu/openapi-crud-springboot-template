package com.xuxiaoye.api.adapter.server.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.adapter.api.server.dto.ResponseStatus;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommonMapper {

    @Mapping(target = "code", expression = "java( status.getCustomizedCode() == null ? status.getCode() : status.getCustomizedCode() )")
    @Mapping(target = "message", source = "message")
    ResponseStatus map(AppStatus status);

}