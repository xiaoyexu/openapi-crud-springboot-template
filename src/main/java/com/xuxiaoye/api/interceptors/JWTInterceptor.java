package com.xuxiaoye.api.interceptors;

import java.util.*;

import com.github.benmanes.caffeine.cache.Cache;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.common.exceptions.ForbiddenException;
import com.xuxiaoye.api.common.exceptions.JWTExpiredException;
import com.xuxiaoye.api.conf.ResourceConfig;
import com.xuxiaoye.api.constant.HeaderConstant;
import com.xuxiaoye.api.utils.JwtUtils;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Log4j2
public class JWTInterceptor implements HandlerInterceptor {
    private final RequestContext requestContext;
    private final ResourceConfig resourceConfig;
    private final Cache<String, Boolean> cache;

    public JWTInterceptor(
            RequestContext requestContext,
            ResourceConfig resourceConfig,
            Cache<String, Boolean> cache
    ) {
        this.requestContext = requestContext;
        this.resourceConfig = resourceConfig;
        this.cache = cache;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String traceId = request.getHeader(HeaderConstant.X_TRACE_ID);
        if (cache.getIfPresent(traceId) != null) {
            throw new ForbiddenException("Duplicated Request");
        }
        cache.put(traceId, true);

        String authorization = request.getHeader(AUTHORIZATION);
        if (StringUtils.isBlank(authorization)) {
            List<SimpleGrantedAuthority> authRoles = List.of(new SimpleGrantedAuthority("ROLE_GUEST"));
            UserDetails userDetails = new User("Guest", "", authRoles);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, "", authRoles);
            SecurityContextHolder.getContext().setAuthentication(auth);
            return true;
        }

        authorization = authorization.replaceAll("^(?i)Bearer(?-i) ", "");

        Claims claims = JwtUtils.validateJWTToken(authorization, resourceConfig.getPublicKey());
        Date expirationTime = claims.getExpiration();
        if (expirationTime.before(new Date())) {
            log.error("JWT Token Expired");
            throw new JWTExpiredException("JWT Token Expired");
        }
        String userId = (String) claims.get("id");
//        String accountName = (String) claims.get("accountName");
        String roles = (String) claims.get("roles");
        String authorities = (String) claims.get("authorities");

        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();

        // Roles
        if (!StringUtils.isBlank(roles)) {
            grantedAuthorities = new java.util.ArrayList<>(Arrays.stream(roles.split(","))
                    .filter(StringUtils::isNotBlank)
                    .map((role) -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .toList()
            );
        }

        // Authorities
        if (!StringUtils.isBlank(authorities)) {
            List<SimpleGrantedAuthority> userAuthorities = new java.util.ArrayList<>(Arrays.stream(authorities.split(","))
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .map(SimpleGrantedAuthority::new)
                    .toList()
            );
            grantedAuthorities.addAll(userAuthorities);
        }

        UserDetails userDetails = new User(userId, "", grantedAuthorities);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, "", grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        this.requestContext.setXUserId(userId);
        this.requestContext.setAuthorization(authorization);
        return true;
    }
}
