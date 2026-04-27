package com.xuxiaoye.api.services;

import java.util.Map;
import java.util.Objects;

import com.xuxiaoye.api.bean.RequestContext;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;

import com.xuxiaoye.api.adapter.api.server.dto.JWT;
import com.xuxiaoye.api.adapter.api.server.dto.LoginRequest;
import com.xuxiaoye.api.bean.TokenPair;
import com.xuxiaoye.api.conf.ResourceConfig;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.services.db.UserDBService;
import com.xuxiaoye.api.services.db.dto.entity.User;
import com.xuxiaoye.api.services.interfaces.UserService;
import com.xuxiaoye.api.utils.JwtUtils;

@Log4j2
public class UserServiceImpl implements UserService {
    private final RequestContext requestContext;
    private final ResourceConfig resourceConfig;
    private final UserDBService userDBService;

    @Value("${admin.accessToken.expiration}")
    private long accessTokenExpiration;

    @Value("${admin.refreshToken.expiration}")
    private long refreshTokenExpiration;

    public UserServiceImpl(
            RequestContext requestContext,
            ResourceConfig resourceConfig,
            UserDBService userDBService
    ) {
        this.requestContext = requestContext;
        this.resourceConfig = resourceConfig;
        this.userDBService = userDBService;
    }

    @Override
    public AppResponse<JWT> login(LoginRequest request) {
        User dbUser = userDBService.getUserByAccountNameAndPassword(
                request.getUsername(),
                JwtUtils.getSHA256(request.getPassword())
        );

        if (dbUser == null) {
            return AppResponse.failWithStatus(AppStatus.badRequest("Invalid access"));
        }

        TokenPair tokenPair = JwtUtils.generateJWTTokenPair(
                resourceConfig.getPrivateKey(),
                this.accessTokenExpiration,
                this.refreshTokenExpiration,
                dbUser.getId(),
                dbUser.getAccountName(),
                buildClaims(dbUser)
        );

        dbUser.setRefreshToken(tokenPair.refreshToken());
        if (!userDBService.updateById(dbUser)) {
            log.error("Fail to save refresh token");
            return AppResponse.failWithStatus(AppStatus.internalError());
        }

        return AppResponse.okWithData(JWT.builder()
                .accessToken(tokenPair.accessToken())
                .refreshToken(tokenPair.refreshToken())
                .expiresIn("" + this.accessTokenExpiration)
                .build());
    }

    @Override
    public AppResponse<String> refresh(String refreshToken) {
        User dbUser = this.userDBService.getUserByIdAndRefreshToken(
                requestContext.getXUserId(),
                refreshToken
        );

        if (dbUser == null) {
            return AppResponse.failWithStatus(AppStatus.badRequest("Invalid access"));
        }

        String newToken = JwtUtils.generateJWTToken(
                resourceConfig.getPrivateKey(),
                dbUser.getId(),
                buildClaims(dbUser),
                this.accessTokenExpiration
        );
        return AppResponse.okWithData(newToken);
    }

    Map<String, Object> buildClaims(User dbUser) {
        return Map.of(
                "id", dbUser.getId(),
                "accountName", dbUser.getAccountName(),
                "roles", Objects.requireNonNullElse(dbUser.getRole(), "")
        );
    }
}
