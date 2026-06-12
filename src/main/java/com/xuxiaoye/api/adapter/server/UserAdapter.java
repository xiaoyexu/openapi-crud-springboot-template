package com.xuxiaoye.api.adapter.server;

import java.util.Arrays;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.api.server.UsersApiDelegate;
import com.xuxiaoye.api.adapter.server.mapper.CommonMapper;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.services.interfaces.UserService;

public class UserAdapter implements UsersApiDelegate {

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
            String xTraceID,
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
    public ResponseEntity<LogoutResponse> logout(
            String xTraceID,
            String authorization
    ) {
        return this.userService.logout().toResponseEntity(
                data -> LogoutResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                status -> LogoutResponse.builder().status(this.commonMapper.map(status)).build()
        );
    }

    @Override
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            String xTraceID,
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

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'user', 'create')")
    public ResponseEntity<CreateUserResponse> createSingleUser(
            String xTraceID,
            String authorization,
            User createUserRequest
    ) {
        return this.userService.create(createUserRequest)
                .toResponseEntity(
                        data -> CreateUserResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> CreateUserResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, #userId, 'user', 'delete') or @P.hasPermission(authentication, #userId, 'user', 'delete_own')")
    public ResponseEntity<DeleteUserResponse> deleteSingleUser(
            String xTraceID,
            String authorization,
            String userId
    ) {
        return this.userService.deleteById(userId)
                .toResponseEntity(
                        data -> DeleteUserResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> DeleteUserResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, #userId, 'user', 'get') or @P.hasPermission(authentication, #userId, 'user', 'get_own')")
    public ResponseEntity<GetUserResponse> getSingleUser(
            String xTraceID,
            String authorization,
            String userId
    ) {
        return this.userService.get(userId)
                .toResponseEntity(
                        data -> GetUserResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> GetUserResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'user', 'search')")
    public ResponseEntity<SearchUserResponse> searchUsers(
            String xTraceID,
            String authorization,
            SearchUserRequest searchUserRequest,
            Integer limit,
            Integer offset,
            String sortBy
    ) {
        return this.userService.search(searchUserRequest, Pagination.of(offset, limit, sortBy))
                .toResponseEntity(
                        data -> SearchUserResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> SearchUserResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, #userId, 'user', 'update') or @P.hasPermission(authentication, #userId, 'user', 'update_own')")
    public ResponseEntity<UpdateUserResponse> updateSingleUser(
            String xTraceID,
            String authorization,
            String userId,
            User updateUserRequest
    ) {
        return this.userService.updateById(userId, updateUserRequest)
                .toResponseEntity(
                        data -> UpdateUserResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> UpdateUserResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'user', 'export')")
    public ResponseEntity<org.springframework.core.io.Resource> exportUsers(
            String xTraceID,
            String authorization,
            SearchUserRequest searchUserRequest,
            Integer limit,
            Integer offset,
            String sortBy
    ) {
        return this.userService.exportData(searchUserRequest, Pagination.of(offset, limit, sortBy), "Users")
                .toFileResponseEntity();
    }

    @Override
    @PreAuthorize("@P.hasPermission(authentication, 'user', 'import')")
    public ResponseEntity<ImportUserResponse> importUsers(
            String xTraceID,
            String authorization,
            MultipartFile file
    ) {
        return this.userService.importData(file)
                .toResponseEntity(
                        data -> ImportUserResponse.builder().data(data).status(this.commonMapper.map(AppStatus.ok())).build(),
                        status -> ImportUserResponse.builder().status(this.commonMapper.map(status)).build()
                );
    }
}
