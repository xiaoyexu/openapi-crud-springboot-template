package com.xuxiaoye.api.adapter.server;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.xuxiaoye.api.adapter.api.server.dto.LoginResponse;
import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.BaseTest;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "bypassTokenCheck=false")
class UserAdapterTest extends BaseTest {
    @LocalServerPort
    private int port;

    @BeforeAll
    public void beforeClass(TestInfo info) {
        RestAssured.baseURI = "http://localhost:" + port;
        log.info("Starting test case {}", info.getDisplayName());
    }

    @Autowired
    ObjectMapper objectMapper;

    @Nested
    @Order(1)
    class LoginTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("/user/login").withMethod("post");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "request.json,result.json",
            })
            void login(String requestJson, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = given().log()
                        .all(true)
                        .headers(BaseTest.HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .basePath(basePath)
                        .body(request)
                        .when()
                        .post("/user/login")
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse)
                        .whenIgnoringPaths("data.accessToken", "data.refreshToken")
                        .isEqualTo(mockRes);
            }
        }

        @Nested
        class Code400 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("400");
            }

            @ParameterizedTest
            @CsvSource({
                    "request.json,bad_request.json",
            })
            void login(String requestJson, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = given().log()
                        .all(true)
                        .headers(BaseTest.HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .basePath(basePath)
                        .body(request)
                        .when()
                        .post("/user/login")
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse)
                        .whenIgnoringPaths("data.accessToken", "data.refreshToken")
                        .isEqualTo(mockRes);
            }

        }
    }

    @Nested
    @Order(2)
    class RefreshTokenTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("/user/refresh").withMethod("post");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "US000003,result.json",
            })
            void refresh(String userId, String responseJson) throws IOException {
                String request = reader.withBase("requests").withEndPoint("user/login").withFileName("request.json").getContent();

                String jsonResponse = given().log()
                        .all(true)
                        .headers(BaseTest.HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .basePath(basePath)
                        .body(request)
                        .when()
                        .post("/user/login")
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse)
                        .whenIgnoringPaths("data.accessToken", "data.refreshToken")
                        .isEqualTo(mockRes);

                LoginResponse loginResponse = objectMapper.readValue(jsonResponse, LoginResponse.class);

                jsonResponse = given().log()
                        .all(true)
                        .headers(BaseTest.HeaderBuilder.defaultHeader()
                                .userId(userId)
                                .authorizationToken(loginResponse.getData().getRefreshToken())
                                .build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .basePath(basePath)
                        .when()
                        .post("/user/refresh")
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();

                mockRes = reader.withBase("responses").withEndPoint("user/refresh").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse)
                        .whenIgnoringPaths("data")
                        .isEqualTo(mockRes);
            }
        }

        @Nested
        class Code401 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("401");
            }

            @ParameterizedTest
            @CsvSource({
                    "unauthorized.json",
            })
            void refresh(String responseJson) throws IOException {
                String jsonResponse = given().log()
                        .all(true)
                        .headers(BaseTest.HeaderBuilder.defaultHeader()
                                .authorizationToken("a.b.c")
                                .build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .basePath(basePath)
                        .when()
                        .post("/user/refresh")
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.UNAUTHORIZED.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }

        }
    }
}