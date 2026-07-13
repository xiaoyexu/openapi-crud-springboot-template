package com.xuxiaoye.api.conf;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import com.xuxiaoye.api.interceptors.JWTAuthenticationFilter;

public class WebSecurityConfig {

    private static final String[] AUTH_WHITELIST = {
            "/users/login",
            "/users/refresh",
            "/api-docs/**",
            "/swagger*/**",
            "/ping",
            "/info"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JWTAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // Need to add below for SSE, becase SSE will return 200 first, then start ASYNC Dispatch
                        // If you don't add this, the request will be blocked by Spring Security before it can reach the ASYNC dispatch
                        .dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                .exceptionHandling(ex -> ex
//                        // 未登录/Token无效 -> 401
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
//                            response.setContentType("application/json;charset=UTF-8");
//                            response.getWriter().write("""
//                                        {"status":{"code":"401","message":"Unauthorized"}}
//                                    """);
//                        })
//                        // 已登录但无权限 -> 403
//                        .accessDeniedHandler((request, response, accessDeniedException) -> {
//                            response.setStatus(HttpStatus.FORBIDDEN.value());
//                            response.setContentType("application/json;charset=UTF-8");
//                            response.getWriter().write("""
//                                        {"status":{"code":"403","message":"Forbidden"}}
//                                    """);
//                        })
//                )
                .build();
    }
}
