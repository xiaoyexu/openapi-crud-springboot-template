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
import org.springframework.test.context.TestPropertySource;

import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.BaseTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "bypassTokenCheck=false")
public class RoleAdapterAuthTest extends BaseTest {
    @LocalServerPort
    private int port;

    @BeforeAll
    public void beforeClass() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @Nested
    @Order(1)
    class GetSingleRoleTest {
        @ParameterizedTest
        @CsvSource(value = {
                "RO000002;user;;role:get_own;200",
                "RO000002;US000001;;role:get_own;401",
                "id_not_exist;US000001;;role:get_own;401"
        }, delimiter = ';')
        public void testGetSingleRoleWithValidToken(String id, String userId, String roles, String authorities, int expectHttpStatus) {
            get(
                    "/roles/" + id,
                    HeaderBuilder.defaultHeader().authorizationToken(buildToken(userId, roles, authorities)).build(),
                    HttpStatus.valueOf(expectHttpStatus).value()
            );
        }
    }

    @Nested
    @Order(2)
    class CreateSingleRoleTest {

        @BeforeEach
        void before() {
            reader = reader.withEndPoint("roles").withMethod("post").withHttpStatus("200");
        }

        @ParameterizedTest
        @CsvSource(value = {
                ";admin;;200",
                ";;role:create;200",
                ";guest;;401",
                ";member;;401",
                ";other;;401",
                ";other;role:create;200"
        }, delimiter = ';')
        public void testCreateSingleRoleWithValidToken(String userId, String roles, String authorities, int expectHttpStatus) throws IOException {
            String request = reader.withBase("requests").withFileName("create.json").getContent();

            post(
                    "/roles",
                    HeaderBuilder.defaultHeader().authorizationToken("Bearer " + buildToken(userId, roles, authorities)).build(),
                    request,
                    HttpStatus.valueOf(expectHttpStatus).value()
            );
        }
    }

    @Nested
    @Order(3)
    class UpdateSingleRoleTest {

        @BeforeEach
        void before() {
            reader = reader.withEndPoint("roles").withMethod("put").withHttpStatus("200");
        }

        @ParameterizedTest
        @CsvSource(value = {
                "RO000002;;admin;;200",
                "RO000002;;;;401",
                "RO000002;;;role:update;200",
                "RO000002;US000002;;role:update;200",
                "RO000002;US000001;;role:update_own;401"
        }, delimiter = ';')
        public void testUpdateSingleRoleWithValidToken(String id, String userId, String roles, String authorities, int expectHttpStatus) throws IOException {
            String request = reader.withBase("requests").withFileName("update.json").getContent();

            put(
                    "/roles/" + id,
                    HeaderBuilder.defaultHeader().authorizationToken("Bearer " + buildToken(userId, roles, authorities)).build(),
                    request,
                    HttpStatus.valueOf(expectHttpStatus).value()
            );
        }
    }

    @Nested
    @Order(4)
    class DeleteSingleRoleTest {

        @BeforeEach
        void before() {
            reader = reader.withEndPoint("roles").withMethod("delete").withHttpStatus("200");
        }

        @ParameterizedTest
        @CsvSource(value = {
                "RO000002;;;;401",
                "RO000002;US000001;;role:delete_own;401",
//                "RO000002;;admin;;200",
                "RO000002;;;role:delete;200",
//                "RO000002;US000002;;role:delete;200",
        }, delimiter = ';')
        public void testDeleteSingleRoleWithValidToken(String id, String userId, String roles, String authorities, int expectHttpStatus) throws IOException {
            delete(
                    "/roles/" + id,
                    HeaderBuilder.defaultHeader().authorizationToken("Bearer " + buildToken(userId, roles, authorities)).build(),
                    HttpStatus.valueOf(expectHttpStatus).value()
            );
        }
    }
}