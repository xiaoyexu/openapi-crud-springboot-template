package com.xuxiaoye.api;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

import io.restassured.http.Cookies;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.xuxiaoye.api.bean.TokenPair;
import com.xuxiaoye.api.conf.ResourceConfig;
import com.xuxiaoye.api.utils.JwtUtils;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
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
//            return String.format("/%s/%s/%s/%s/%s/%s", base, version, endPoint, method, httpStatus, fileName);
            return String.format("/apis/%s/%s/%s/%s/%s/%s", version, endPoint, method, httpStatus, base, fileName);
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
            builder.traceId = UUID.randomUUID().toString();
            builder.contextId = UUID.randomUUID().toString();
            builder.env = "env";
            builder.lbu = "application";
            builder.authorizationUser = "12345";
            builder.authorizationApp = "app";
            builder.authorizationToken = "";
            builder.forwardedFor = "192.168.0.10";
            builder.acceptLanguage = "zh_HK";
            builder.deviceId = "12345678";
            builder.userId = "US000001";
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

    protected Headers defaultHeader() {
        return HeaderBuilder.defaultHeader()
                .authorizationToken(buildToken("US000001", "ADMIN", ""))
                .build();
    }

    protected String post(String urlPath, String requestBody, int httpStatus, Object... queryParams) {
        return post(urlPath, defaultHeader(), requestBody, httpStatus, queryParams);
    }

    protected String post(String urlPath, Headers headers, String requestBody, int httpStatus, Object... queryParams) {
        return post(urlPath, headers, null, requestBody, httpStatus, queryParams);
    }

    protected String post(String urlPath, Headers headers, Cookies cookies, String requestBody, int httpStatus, Object... queryParams) {
        return postExtractable(urlPath, headers, cookies, requestBody, httpStatus, queryParams).asString();
    }

    protected ExtractableResponse<Response> postExtractable(String urlPath, Headers headers, Cookies cookies, String requestBody, int httpStatus, Object... queryParams) {
        RequestSpecification spec = given();

        if (cookies != null) {
            spec.cookies(cookies);
        }

        spec.log()
                .all(true)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .basePath(this.basePath)
                .body(requestBody);

        IntStream.range(0, queryParams.length / 2)
                .map(i -> i * 2)
                .filter(i -> queryParams[i + 1] != null)
                .forEach(i -> spec.queryParams(queryParams[i].toString(), queryParams[i + 1]));

        return spec
                .when()
                .post(urlPath)
                .then()
                .log()
                .all(true)
                .assertThat()
                .statusCode(httpStatus)
                .extract();
    }

    protected String postAcceptAny(String urlPath, String requestBody, int httpStatus, Object... queryParams) {
        return postAcceptAny(urlPath, defaultHeader(), requestBody, httpStatus, queryParams);
    }

    protected String postAcceptAny(String urlPath, Headers headers, String requestBody, int httpStatus, Object... queryParams) {
        RequestSpecification spec = given().log()
                .all(true)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .basePath(this.basePath)
                .body(requestBody);

        IntStream.range(0, queryParams.length / 2)
                .map(i -> i * 2)
                .filter(i -> queryParams[i + 1] != null)
                .forEach(i -> spec.queryParams(queryParams[i].toString(), queryParams[i + 1]));

        return spec
                .when()
                .post(urlPath)
                .then()
                .log()
                .all(true)
                .assertThat()
                .statusCode(httpStatus)
                .extract()
                .asString();
    }

    protected String postFile(String urlPath, String name, File file, int httpStatus) {
        return postFile(urlPath, defaultHeader(), name, file, httpStatus);
    }

    protected String postFile(String urlPath, String name, File file, int httpStatus, Object... formParams) {
        return postFile(urlPath, defaultHeader(), name, file, httpStatus, formParams);
    }

    protected String postFile(String urlPath, Headers headers, String name, File file, int httpStatus) {
        return given().log()
                .all(true)
                .headers(headers)
                .basePath(basePath)
                .multiPart(name, file)
                .when()
                .post(urlPath)
                .then()
                .log()
                .all(true)
                .assertThat()
                .statusCode(httpStatus)
                .extract()
                .asString();
    }

    protected String postFile(String urlPath, Headers headers, String name, File file, int httpStatus, Object... formParams) {
        RequestSpecification spec = given().log()
                .all(true)
                .headers(headers)
                .basePath(this.basePath)
                .multiPart(name, file);

        IntStream.range(0, formParams.length / 2)
                .map(i -> i * 2)
                .filter(i -> formParams[i + 1] != null)
                .forEach(i -> spec.formParams(formParams[i].toString(), formParams[i + 1]));

        return spec
                .when()
                .post(urlPath)
                .then()
                .log()
                .all(true)
                .assertThat()
                .statusCode(httpStatus)
                .extract()
                .asString();
    }

    protected String put(String urlPath, String requestBody, int httpStatus, Object... queryParams) {
        return put(urlPath, defaultHeader(), requestBody, httpStatus, queryParams);
    }

    protected String put(String urlPath, Headers headers, String requestBody, int httpStatus, Object... queryParams) {
        RequestSpecification spec = given().log()
                .all(true)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .basePath(this.basePath)
                .body(requestBody);

        IntStream.range(0, queryParams.length / 2)
                .map(i -> i * 2)
                .filter(i -> queryParams[i + 1] != null)
                .forEach(i -> spec.queryParams(queryParams[i].toString(), queryParams[i + 1]));

        return spec
                .when()
                .put(urlPath)
                .then()
                .log()
                .all(true)
                .assertThat()
                .statusCode(httpStatus)
                .extract()
                .asString();
    }

    protected String get(String urlPath, int httpStatus, Object... queryParams) {
        return get(urlPath, defaultHeader(), httpStatus, queryParams);
    }

    protected String get(String urlPath, Headers headers, int httpStatus, Object... queryParams) {
        RequestSpecification spec = given().log()
                .all(true)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .basePath(this.basePath);

        IntStream.range(0, queryParams.length / 2)
                .map(i -> i * 2)
                .filter(i -> queryParams[i + 1] != null)
                .forEach(i -> spec.queryParams(queryParams[i].toString(), queryParams[i + 1]));

        return spec
                .when()
                .get(urlPath)
                .then()
                .log()
                .all(true)
                .assertThat()
                .statusCode(httpStatus)
                .extract()
                .asString();
    }

    protected String getAcceptAny(String urlPath, int httpStatus, Object... queryParams) {
        return getAcceptAny(urlPath, defaultHeader(), httpStatus, queryParams);
    }

    protected String getAcceptAny(String urlPath, Headers headers, int httpStatus, Object... queryParams) {
        RequestSpecification spec = given().log()
                .all(true)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .basePath(this.basePath);

        IntStream.range(0, queryParams.length / 2)
                .map(i -> i * 2)
                .filter(i -> queryParams[i + 1] != null)
                .forEach(i -> spec.queryParams(queryParams[i].toString(), queryParams[i + 1]));

        return spec
                .when()
                .get(urlPath)
                .then()
                .log()
                .all(true)
                .assertThat()
                .statusCode(httpStatus)
                .extract()
                .asString();
    }

    protected String delete(String urlPath, int httpStatus, Object... queryParams) {
        return delete(urlPath, defaultHeader(), httpStatus, queryParams);
    }

    protected String delete(String urlPath, Headers headers, int httpStatus, Object... queryParams) {
        RequestSpecification spec = given().log()
                .all(true)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .basePath(this.basePath);

        IntStream.range(0, queryParams.length / 2)
                .map(i -> i * 2)
                .filter(i -> queryParams[i + 1] != null)
                .forEach(i -> spec.queryParams(queryParams[i].toString(), queryParams[i + 1]));

        return spec
                .when()
                .delete(urlPath)
                .then()
                .log()
                .all(true)
                .assertThat()
                .statusCode(httpStatus)
                .extract()
                .asString();
    }
}

