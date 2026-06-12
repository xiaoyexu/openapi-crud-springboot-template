package com.xuxiaoye.api.interceptors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.github.benmanes.caffeine.cache.Cache;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.constant.HeaderConstant;
import com.xuxiaoye.api.conf.ResourceConfig;
import com.xuxiaoye.api.utils.JwtUtils;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final RequestContext requestContext;
    private final ResourceConfig resourceConfig;
    private final Cache<String, Boolean> cache;

    public JWTAuthenticationFilter(
            RequestContext requestContext,
            ResourceConfig resourceConfig,
            Cache<String, Boolean> cache
    ) {
        this.requestContext = requestContext;
        this.resourceConfig = resourceConfig;
        this.cache = cache;
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    )
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateRequest(request);
        }

        filterChain.doFilter(request, response);
    }

    protected void authenticateRequest(HttpServletRequest request) {
        String traceId = request.getHeader(HeaderConstant.X_TRACE_ID);
        if (cache.getIfPresent(traceId) != null) {
            throw new RuntimeException("Duplicated Request");
        }
        cache.put(traceId, true);

        String authorization = request.getHeader(AUTHORIZATION);

        if (StringUtils.isBlank(authorization)) {
            setGuestAuthentication();
            return;
        }

        try {
            authorization = authorization.replaceAll("^(?i)Bearer(?-i) ", "");
            Claims claims = JwtUtils.validateJWTToken(authorization, resourceConfig.getPublicKey());

            Date expirationTime = claims.getExpiration();
            if (expirationTime.before(new Date())) {
                throw new RuntimeException("JWT Token Expired");
            }

            String userId = (String) claims.get("id");
            String roles = (String) claims.get("roles");
            String authorities = (String) claims.get("authorities");

            List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();

            if (StringUtils.isNotBlank(roles)) {
                grantedAuthorities.addAll(Arrays.stream(roles.split(","))
                        .filter(StringUtils::isNotBlank)
                        .map((role) -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .toList());
            }

            if (StringUtils.isNotBlank(authorities)) {
                grantedAuthorities.addAll(Arrays.stream(authorities.split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .map(SimpleGrantedAuthority::new)
                        .toList());
            }

            UserDetails userDetails = new User(userId, "", grantedAuthorities);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, "", grantedAuthorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            this.requestContext.setXUserId(userId);
            this.requestContext.setAuthorization(authorization);
        } catch (RuntimeException ex) {
            throw new BadCredentialsException("Invalid Authorization token", ex);
        }
    }

    private static void setGuestAuthentication() {
        List<SimpleGrantedAuthority> guestRoles = List.of(new SimpleGrantedAuthority("ROLE_GUEST"));
        UserDetails guestUser = new User("Guest", "", guestRoles);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(guestUser, "", guestRoles);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}


