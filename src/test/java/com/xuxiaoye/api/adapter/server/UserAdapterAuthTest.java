package com.xuxiaoye.api.adapter.server;

import java.io.IOException;

import io.restassured.RestAssured;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.BaseTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(11)
public class UserAdapterAuthTest extends BaseTest {
    @LocalServerPort
    private int port;

    @BeforeAll
    public void beforeClass() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @Nested
    @Order(1)
    class GetSingleUserTest {
        @ParameterizedTest
        @CsvSource(value = {
                "US000002;user;;user:get_own;200",
                "US000002;US000001;;user:get_own;401",
                "id_not_exist;US000001;;user:get_own;401"
        }, delimiter = ';')
        public void testGetSingleUserWithValidToken(String id, String userId, String roles, String authorities, int expectHttpStatus) {
            get(
                    "/users/" + id,
                    HeaderBuilder.defaultHeader().authorizationToken(buildToken(userId, roles, authorities)).build(),
                    HttpStatus.valueOf(expectHttpStatus).value()
            );
        }
    }

    @Nested
    @Order(2)
    class CreateSingleUserTest {

        @BeforeEach
        void before() {
            reader = reader.withEndPoint("users").withMethod("post").withHttpStatus("200");
        }

        @ParameterizedTest
        @CsvSource(value = {
                ";admin;;200",
                ";;user:create;200",
                ";guest;;401",
                ";member;;401",
                ";other;;401",
                ";other;user:create;200"
        }, delimiter = ';')
        public void testGetSingleUserWithValidToken(String userId, String roles, String authorities, int expectHttpStatus) throws IOException {
            String request = "{\n" +
                    "  \"accountName\": \"acc" + easyRandom.nextObject(String.class) + "\",\n" +
                    "  \"role\": \"ADMIN\",\n" +
                    "  \"createdBy\": \"system\",\n" +
                    "  \"createdAt\": \"2025-01-01T09:00:00\",\n" +
                    "  \"updatedBy\": \"user\",\n" +
                    "  \"updatedAt\": \"2025-02-01T09:00:00\"\n" +
                    "}";

            post(
                    "/users",
                    HeaderBuilder.defaultHeader().authorizationToken(buildToken(userId, roles, authorities)).build(),
                    request,
                    HttpStatus.valueOf(expectHttpStatus).value()
            );
        }
    }

    @Nested
    @Order(3)
    class UpdateSingleUserTest {

        @BeforeEach
        void before() {
            reader = reader.withEndPoint("users").withMethod("put").withHttpStatus("200");
        }

        @ParameterizedTest
        @CsvSource(value = {
                "US000002;;admin;;200",
                "US000002;;;;401",
                "US000002;;;user:update;200",
                "US000002;US000002;;user:update;200",
                "US000002;US000001;;user:update_own;401"
        }, delimiter = ';')
        public void testGetSingleUserWithValidToken(String id, String userId, String roles, String authorities, int expectHttpStatus) throws IOException {
            String request = reader.withBase("requests").withFileName("update.json").getContent();

            put(
                    "/users/" + id,
                    HeaderBuilder.defaultHeader().authorizationToken(buildToken(userId, roles, authorities)).build(),
                    request,
                    HttpStatus.valueOf(expectHttpStatus).value()
            );
        }
    }

    @Nested
    @Order(4)
    class DeleteSingleUserTest {

        @BeforeEach
        void before() {
            reader = reader.withEndPoint("users").withMethod("delete").withHttpStatus("200");
        }

        @ParameterizedTest
        @CsvSource(value = {
                "US000002;;admin;;200",
                "US000002;;;;401",
//                "US000002;;;user:delete;200",
//                "US000002;US000002;;user:delete;200",
                "US000002;US000001;;user:delete_own;401"
        }, delimiter = ';')
        public void testGetSingleUserWithValidToken(String id, String userId, String roles, String authorities, int expectHttpStatus) throws IOException {
//            String request = reader.withBase("requests").withFileName("update.json").getContent();

            delete(
                    "/users/" + id,
                    HeaderBuilder.defaultHeader().authorizationToken(buildToken(userId, roles, authorities)).build(),
                    HttpStatus.valueOf(expectHttpStatus).value()
            );
        }
    }
}