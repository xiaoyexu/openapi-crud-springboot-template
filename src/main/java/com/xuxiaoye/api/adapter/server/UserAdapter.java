package com.xuxiaoye.api.adapter.server;

import org.springframework.http.ResponseEntity;

import com.xuxiaoye.api.adapter.api.server.UserApiDelegate;
import com.xuxiaoye.api.adapter.api.server.dto.LoginRequest;
import com.xuxiaoye.api.adapter.api.server.dto.LoginResponse;
import com.xuxiaoye.api.adapter.api.server.dto.RefreshTokenResponse;
import com.xuxiaoye.api.adapter.server.mapper.CommonMapper;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.services.interfaces.UserService;

public class UserAdapter implements UserApiDelegate {

    private final CommonMapper commonMapper;
    private final UserService userService;

    public UserAdapter(
            CommonMapper commonMapper,
            UserService userService
    ) {
        this.commonMapper = commonMapper;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<LoginResponse> login(
            LoginRequest loginRequest
    ) {
        return this.userService.login(loginRequest).toResponseEntity(
                data -> LoginResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                status -> LoginResponse.builder().status(this.commonMapper.map(status)).build()
        );
    }

    @Override
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            String authorization
    ) {
        return this.userService.refresh(authorization).toResponseEntity(
                data -> RefreshTokenResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                status -> RefreshTokenResponse.builder().status(this.commonMapper.map(status)).build()
        );
    }
}
