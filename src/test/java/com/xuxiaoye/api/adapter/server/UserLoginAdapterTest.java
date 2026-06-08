package com.xuxiaoye.api.adapter.server;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.http.Cookies;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.BaseTest;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(800)
class UserLoginAdapterTest extends BaseTest {
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
            reader = reader.withEndPoint("/users/login").withMethod("post");
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

                String jsonResponse = post(
                        "/users/login",
                        request,
                        HttpStatus.OK.value()
                );

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

                String jsonResponse = post(
                        "/users/login",
                        request,
                        HttpStatus.BAD_REQUEST.value()
                );

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
            reader = reader.withEndPoint("/users/refresh").withMethod("post");
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
                String request = reader.withBase("requests")
                        .withEndPoint("users/login")
                        .withFileName("request.json").getContent();

                ExtractableResponse<Response> response = postExtractable(
                        "/users/login",
                        HeaderBuilder.defaultHeader().build(),
                        null,
                        request,
                        HttpStatus.OK.value()
                );
                String jsonResponse = response.asString();
                String refreshToken = response.cookie("refresh_token");

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse)
                        .whenIgnoringPaths("data.accessToken", "data.refreshToken")
                        .isEqualTo(mockRes);

                Cookie cookie = new Cookie.Builder("refresh_token", refreshToken).build();

                jsonResponse = post(
                        "/users/refresh",
                        HeaderBuilder.defaultHeader().userId(userId).build(),
                        new Cookies(List.of(cookie)),
                        request,
                        HttpStatus.OK.value()
                );

                mockRes = reader.withBase("responses").withEndPoint("users/refresh").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse)
                        .whenIgnoringPaths("data")
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
                    "bad_request.json",
            })
            void refresh(String responseJson) throws IOException {
                String request = reader.withBase("requests")
                        .withEndPoint("users/login")
                        .withHttpStatus("200")
                        .withFileName("request.json").getContent();

                post(
                        "/users/login",
                        request,
                        HttpStatus.OK.value()
                );
                String jsonResponse = post(
                        "/users/refresh",
                        request,
                        HttpStatus.BAD_REQUEST.value()
                );

                String mockRes = reader.withBase("responses")
                        .withEndPoint("users/refresh")
                        .withHttpStatus("400")
                        .withFileName(responseJson).getContent();
                assertThatJson(jsonResponse)
                        .whenIgnoringPaths("data")
                        .isEqualTo(mockRes);
            }
        }
    }
}