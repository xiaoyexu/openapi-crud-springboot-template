package com.xuxiaoye.api.adapter.server;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.xuxiaoye.api.adapter.api.server.UserApiDelegate;
import com.xuxiaoye.api.adapter.api.server.dto.LoginRequest;
import com.xuxiaoye.api.adapter.api.server.dto.LoginResponse;
import com.xuxiaoye.api.adapter.api.server.dto.RefreshTokenResponse;
import com.xuxiaoye.api.adapter.server.mapper.CommonMapper;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.services.interfaces.UserService;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

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
                data -> LoginResponse.builder().data(this.commonMapper.map(data)).status(this.commonMapper.map(AppStatus.ok())).build(),
                status -> LoginResponse.builder().status(this.commonMapper.map(status)).build(),
                (data) -> {
                    String setCookie = String.format("refresh_token=%s; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=%d",
                            data.getRefreshToken(),
                            7 * 24 * 60 * 60
                    );
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Set-Cookie", setCookie);
                    return headers;
                }
        );
    }

    @Override
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            String authorization
    ) {
        String refreshToken = extractRefreshToken();
        return this.userService.refresh(refreshToken).toResponseEntity(
                data -> RefreshTokenResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                status -> RefreshTokenResponse.builder().status(this.commonMapper.map(status)).build()
        );
    }

    private static String extractRefreshToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = attributes.getRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        return Arrays.stream(cookies).filter(cookie ->
                        "refresh_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse("");
    }
}
