package com.xuxiaoye.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.jeasy.random.EasyRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.xuxiaoye.api.bean.TokenPair;
import com.xuxiaoye.api.conf.ResourceConfig;
import com.xuxiaoye.api.utils.JwtUtils;

public abstract class BaseTest {
    protected BaseTest.JsonFileReader reader = new BaseTest.JsonFileReader().withVersion("v1");

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    ResourceConfig resourceConfig;

    @Value("${spring.mvc.servlet.path}")
    protected String basePath;

    protected EasyRandom easyRandom = new EasyRandom();

    public class JsonFileReader {
        private String base;
        private String version;
        private String endPoint;
        private String method;
        private String httpStatus;
        private String fileName;

        public JsonFileReader withBase(String base) {
            this.base = base;
            return this;
        }

        public JsonFileReader withVersion(String version) {
            this.version = version;
            return this;
        }

        public JsonFileReader withEndPoint(String endPoint) {
            this.endPoint = endPoint;
            return this;
        }

        public JsonFileReader withMethod(String method) {
            this.method = method;
            return this;
        }

        public JsonFileReader withHttpStatus(String httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public JsonFileReader withFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public String getContent() throws IOException {
            if (this.base == null) {
                throw new IllegalArgumentException("missing base value");
            }
            if (this.version == null) {
                throw new IllegalArgumentException("missing version value");
            }
            if (this.endPoint == null) {
                throw new IllegalArgumentException("missing endPoint value");
            }
            if (this.method == null) {
                throw new IllegalArgumentException("missing method value");
            }
            if (this.httpStatus == null) {
                throw new IllegalArgumentException("missing httpStatus value");
            }
            if (this.fileName == null) {
                throw new IllegalArgumentException("missing fileName value");
            }
            return resourceLoader.getResource("classpath:" + getPath())
                    .getContentAsString(StandardCharsets.UTF_8);
        }

        public String getPath() {
            return String.format("/%s/%s/%s/%s/%s/%s", base, version, endPoint, method, httpStatus, fileName);
        }

        public Resource getResource() {
            return resourceLoader.getResource("classpath:" + getPath());
        }
    }

    public static class HeaderBuilder {
        private String traceId;
        private String contextId;
        private String authorizationUser;
        private String authorizationApp;
        private String authorizationToken;
        private String lbu;
        private String env;
        private String forwardedFor;
        private String acceptLanguage;
        private String deviceId;
        private String userId;

        public HeaderBuilder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public HeaderBuilder contextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public HeaderBuilder authorizationUser(String authorizationUser) {
            this.authorizationUser = authorizationUser;
            return this;
        }

        public HeaderBuilder authorizationApp(String authorizationApp) {
            this.authorizationApp = authorizationApp;
            return this;
        }

        public HeaderBuilder authorizationToken(String authorizationToken) {
            this.authorizationToken = authorizationToken;
            return this;
        }

        public HeaderBuilder lbu(String lbu) {
            this.lbu = lbu;
            return this;
        }

        public HeaderBuilder env(String env) {
            this.env = env;
            return this;
        }

        public HeaderBuilder forwardedFor(String forwardedFor) {
            this.forwardedFor = forwardedFor;
            return this;
        }

        public HeaderBuilder acceptLanguage(String acceptLanguage) {
            this.acceptLanguage = acceptLanguage;
            return this;
        }

        public HeaderBuilder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public HeaderBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public static HeaderBuilder defaultHeader() {
            HeaderBuilder builder = new HeaderBuilder();
            builder.traceId = "kHsnH02437";
            builder.contextId = "CB3gT";
            builder.env = "env";
            builder.lbu = "application";
            builder.authorizationUser = "12345";
            builder.authorizationApp = "app";
            builder.authorizationToken = "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6IlhvTTVOZjhMWjVjRUppMXNPRURsSUVmMUNXd3d0aUlMcjFXb1FrblVaNDgiLCJ0eXAiOiJKV1QifQ.eyJzdWIiOiJlMzQ0YjNjOS1jYWNjLTQzMGUtOTk4Ni04NWFmYmRmMzIzNGQiLCJvaWQiOiJlMzQ0YjNjOS1jYWNjLTQzMGUtOTk4Ni04NWFmYmRmMzIzNGQiLCJpc01mYUxvZ2luIjpmYWxzZSwic3NvSWQiOiJCOTEwNDI4NjU5IiwicGFydHlJZCI6IkI5MTA0Mjg2NTkiLCJ2dElkIjoiVjgwMDEzMjkyOSIsImNjSWQiOiIwNzUyMjg1OTUiLCJoYXNQaWkiOnRydWUsImxlSWQiOiIiLCJscElkIjoiIiwiYWNyIjoiQjJDXzFBX1NlYW1sZXNzTWlncmF0aW9uX3NpZ251cG9yc2lnbmluIiwiaGFzX3BhcnR5X2lkIjoiWSIsIm5vbmNlIjoiZGVmYXVsdE5vbmNlIiwiYXpwIjoiZmYwYWVkZDktOGFlOC00NzlkLWI4OGYtYWM4OTQ4MDAwNjdkIiwidmVyIjoiMS4wIiwiaWF0IjoxNjkwNzkzNzU0LCJhdWQiOiJmZjBhZWRkOS04YWU4LTQ3OWQtYjg4Zi1hYzg5NDgwMDA2N2QiLCJleHAiOjE2OTA3OTU1NTQsImlzcyI6Imh0dHBzOi8vYWlhaGtucC5iMmNsb2dpbi5jb20vNGI0NzNkYjgtOWMwOC00MzU1LWI3YjEtNjJkNzM1NGNkMzI4L3YyLjAvIiwibmJmIjoxNjkwNzkzNzU0fQ.Al-hxjczmIyK8b8sbwYspJN-y5kNGC9pJE2ZXPb9Io1_EiMcOauYK4B4Jj_GbYVbjcAqcizJbMzq3oTF98oCKuQmi8r5T5RETu8SD2kER9EwKC1eoONeZV8IZeD2pjRtac9owe7qGM-Pl34nr01p8MSce20DE2DDTtMikVSCamdtF8X7imEXR8p1E60QluTyu-n4eNU7NyXy1qeDhC4RurclmhH4b3kzKzIi730DYs35-y0OPH6HbyatjWaDdSk3zvaSL5r_p5DQAaSF_kTrNmn6_Pnrq8x-rzbjNaXQHY5W_afW0Z5na9Lftcfv4ZgMuqP1yuhO6Qs5WiOh_xDSmg";
            builder.forwardedFor = "192.168.0.10";
            builder.acceptLanguage = "zh_HK";
            builder.deviceId = "12345678";
            builder.userId = "US000002";
            return builder;
        }

        public Headers build() {
            List<Header> lHeaders = new ArrayList<Header>();

            if (Objects.nonNull(this.traceId)) {
                lHeaders.add(new Header("x-Trace-ID", this.traceId));
            }

            if (Objects.nonNull(this.contextId)) {
                lHeaders.add(new Header("x-Context-ID", this.contextId));
            }

            if (Objects.nonNull(this.authorizationUser)) {
                lHeaders.add(new Header("x-authorization-user", this.authorizationUser));
            }

            if (Objects.nonNull(this.authorizationApp)) {
                lHeaders.add(new Header("x-authorization-app", this.authorizationApp));
            }

            if (Objects.nonNull(this.authorizationToken)) {
                lHeaders.add(new Header("Authorization", this.authorizationToken));
            }

            if (Objects.nonNull(this.lbu)) {
                lHeaders.add(new Header("x-lbu", this.lbu));
            }

            if (Objects.nonNull(this.acceptLanguage)) {
                lHeaders.add(new Header("Accept-Language", this.acceptLanguage));
            }

            if (Objects.nonNull(this.env)) {
                lHeaders.add(new Header("x-env", this.env));
            }

            if (Objects.nonNull(this.forwardedFor)) {
                lHeaders.add(new Header("X-Forwarded-For", this.forwardedFor));
            }

            if (Objects.nonNull(this.deviceId)) {
                lHeaders.add(new Header("x-device-id", this.deviceId));
            }

            if (Objects.nonNull(this.userId)) {
                lHeaders.add(new Header("x-user-id", this.userId));
            }

            return new Headers(lHeaders);
        }
    }

    protected String getContent(String path) throws IOException {
        return resourceLoader.getResource("classpath:" + path)
                .getContentAsString(StandardCharsets.UTF_8);
    }

    protected String buildToken(String userId, String roles, String authorities) {
        if (userId == null) {
            userId = easyRandom.nextObject(String.class);
        }
        TokenPair tokenPair = JwtUtils.generateJWTTokenPair(
                resourceConfig.getPrivateKey(),
                180,
                180,
                userId,
                easyRandom.nextObject(String.class),
                Map.of(
                        "id", userId,
                        "accountName", easyRandom.nextObject(String.class),
                        "roles", roles == null ? "" : roles,
                        "authorities", authorities == null ? "" : authorities
                )
        );
        return tokenPair.accessToken();
    }
}

