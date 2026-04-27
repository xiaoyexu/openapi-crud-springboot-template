package com.xuxiaoye.api.services.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import com.xuxiaoye.api.services.db.dto.entity.User;

@Mapper
@SuppressWarnings("java:S1172")
public interface UserDBMapper extends BaseMapper<User> {
}
