package com.xuxiaoye.api.services.interfaces;

import com.xuxiaoye.api.adapter.api.server.dto.LoginRequest;
import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.UserMapper;
import com.xuxiaoye.api.services.db.UserDBService;
import com.xuxiaoye.api.resp.AppResponse;

public interface UserService extends Service<String, User, SearchUserRequest, PagedUsers, UserMapper, UserDBService> {
    AppResponse<com.xuxiaoye.api.bean.JWT> login(LoginRequest request);

    AppResponse<String> logout();

    AppResponse<String> refresh(String refreshToken);
}
