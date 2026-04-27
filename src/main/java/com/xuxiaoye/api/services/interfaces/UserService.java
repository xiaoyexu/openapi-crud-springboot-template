package com.xuxiaoye.api.services.interfaces;

import com.xuxiaoye.api.adapter.api.server.dto.JWT;
import com.xuxiaoye.api.adapter.api.server.dto.LoginRequest;
import com.xuxiaoye.api.resp.AppResponse;

public interface UserService {
    AppResponse<JWT> login(LoginRequest request);

    AppResponse<String> refresh(String refreshToken);
}
