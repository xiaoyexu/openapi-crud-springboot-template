package com.xuxiaoye.api.services;

import java.lang.Exception;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.jsonwebtoken.Claims;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.dhatim.fastexcel.reader.Row;
import org.springframework.beans.factory.annotation.Value;

import com.xuxiaoye.api.adapter.api.server.dto.*;
import com.xuxiaoye.api.adapter.server.mapper.UserMapper;
import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.bean.TokenPair;
import com.xuxiaoye.api.client.CRUDDbClient;
import com.xuxiaoye.api.conf.ResourceConfig;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.services.db.UserDBService;
import com.xuxiaoye.api.services.db.mapper.UserDBMapper;
import com.xuxiaoye.api.services.interfaces.UserService;
import com.xuxiaoye.api.utils.ExcelHelper;
import com.xuxiaoye.api.utils.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.xuxiaoye.api.client.BaseDbClient.Operator.*;

@Log4j2
public class UserServiceImpl extends CRUDDbClient<
        User,
        SearchUserRequest,
        PagedUsers,
        UserMapper,
        com.xuxiaoye.api.services.db.dto.entity.User,
        UserDBMapper,
        UserDBService
        > implements UserService {

    private final ResourceConfig resourceConfig;

    private final UserMapper userMapper;
    private final UserDBService userDBService;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.accessToken.expiration}")
    private long accessTokenExpiration;

    @Value("${admin.refreshToken.expiration}")
    private long refreshTokenExpiration;

    public UserServiceImpl(
            RequestContext requestContext,
            ResourceConfig resourceConfig,
            UserMapper userMapper,
            UserDBService userDBService,
            PasswordEncoder passwordEncoder
    ) {
        this.requestContext = requestContext;
        this.resourceConfig = resourceConfig;
        this.userMapper = userMapper;
        this.userDBService = userDBService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserMapper getMapper() {
        return this.userMapper;
    }

    @Override
    public UserDBService getDBService() {
        return this.userDBService;
    }

    @Override
    public AppResponse<com.xuxiaoye.api.bean.JWT> login(LoginRequest request) {
        return handleDbCall(() -> {
            com.xuxiaoye.api.services.db.dto.entity.User dbUser = userDBService.getUserByAccountName(
                    request.getUsername()
            );

            if (dbUser == null || !passwordEncoder.matches(request.getPassword(), dbUser.getPasswordHash())) {
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

            return AppResponse.okWithData(com.xuxiaoye.api.bean.JWT.builder()
                    .accessToken(tokenPair.accessToken())
                    .refreshToken(tokenPair.refreshToken())
                    .expiresIn("" + this.accessTokenExpiration)
                    .build());
        });
    }

    @Override
    public AppResponse<String> refresh(String refreshToken) {
        String userId;
        try {
            Claims claims = JwtUtils.validateJWTToken(refreshToken, resourceConfig.getPublicKey());
            userId = (String) claims.get("id");
        } catch (Exception e) {
            return AppResponse.failWithStatus(AppStatus.badRequest("Invalid access"));
        }

        return handleDbCall(() -> {
            com.xuxiaoye.api.services.db.dto.entity.User dbUser = this.userDBService.getUserByIdAndRefreshToken(
                    userId,
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
        });
    }

    Map<String, Object> buildClaims(com.xuxiaoye.api.services.db.dto.entity.User dbUser) {
        return Map.of(
                "id", dbUser.getId(),
                "accountName", dbUser.getAccountName(),
                "roles", Objects.requireNonNullElse(dbUser.getRole(), "")
//                "authorities",Objects.requireNonNullElse(dbUser.getRole(), ""),
//                "permission",Objects.requireNonNullElse(dbUser.getRole(), "")
        );
    }

    @Override
    public LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.User> buildQuery(
            LambdaQueryWrapper<com.xuxiaoye.api.services.db.dto.entity.User> query,
            SearchUserRequest searchUserRequest,
            Pagination pagination
    ) {
        addSortField(
                query,
                pagination,
                sortField -> com.xuxiaoye.api.services.db.dto.entity.User::getId
        );

        // Todo - Add search fields here
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.User::getId, searchUserRequest.getIds());
        addFilter(query, DATETIME_RANGE, com.xuxiaoye.api.services.db.dto.entity.User::getCreatedAt, searchUserRequest.getCreatedAts());
        addFilter(query, DATETIME_RANGE, com.xuxiaoye.api.services.db.dto.entity.User::getUpdatedAt, searchUserRequest.getUpdatedAts());
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.User::getCreatedBy, searchUserRequest.getCreatedBys());
        addFilter(query, IN, com.xuxiaoye.api.services.db.dto.entity.User::getUpdatedBy, searchUserRequest.getUpdatedBys());

        return query;
    }

    @Override
    protected AppResponse<User> validate(User user) {
        if (StringUtils.isBlank(user.getAccountName())) {
            return AppResponse.failWithStatus(AppStatus.badRequest("Missing Account Name"));
        }
        return AppResponse.ok();
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{
                "ACTION", // A - Add, U - Update , D - Delete
                "ID",
                "ACCOUNT NAME",
                "PASSWORD HASHED",
                "ROLE",
                "REFRESH TOKEN",
                // "XXX",
                "CREATED BY",
                "CREATED AT",
                "UPDATED BY",
                "UPDATED AT"
        };
    }

    @Override
    protected void writeRow(ExcelHelper.ExcelWriter excelWriter, int rowIdx, int colIdx, User user) {
        excelWriter.value(rowIdx, colIdx++, "");
        excelWriter.value(rowIdx, colIdx++, user.getId());
        excelWriter.value(rowIdx, colIdx++, user.getAccountName());
        excelWriter.value(rowIdx, colIdx++, user.getPasswordHash());
        excelWriter.value(rowIdx, colIdx++, user.getRole());
        excelWriter.value(rowIdx, colIdx++, user.getRefreshToken());
        excelWriter.value(rowIdx, colIdx++, user.getCreatedBy());
        excelWriter.value(rowIdx, colIdx++, user.getCreatedAt());
        excelWriter.value(rowIdx, colIdx++, user.getUpdatedBy());
        excelWriter.value(rowIdx, colIdx++, user.getUpdatedAt());
    }

    @Override
    protected User buildFromRow(String id, int colIdx, Row row) {
        String accountName = row.getCellAsString(colIdx++).orElse(null);
        String passwordHash = row.getCellAsString(colIdx++).orElse(null);
        String role = row.getCellAsString(colIdx++).orElse(null);
        String refreshToke = row.getCellAsString(colIdx++).orElse(null);
        // default columns
        String createdBy = row.getCellAsString(colIdx++).orElse(this.requestContext.getXUserId());
        String createdAt = row.getCellAsString(colIdx++).orElse(LocalDateTime.now().toString());
        String updatedBy = row.getCellAsString(colIdx++).orElse(this.requestContext.getXUserId());
        String updatedAt = row.getCellAsString(colIdx++).orElse(LocalDateTime.now().toString());

        return User.builder()
                .id(id)
                .accountName(accountName)
                .passwordHash(passwordHash)
                .role(role)
                .refreshToken(refreshToke)
                // default columns
                .createdBy(createdBy)
                .createdAt(createdAt)
                .updatedBy(updatedBy)
                .updatedAt(updatedAt)
                .build();
    }
}
