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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.BaseTest;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestPropertySource(properties = "bypassTokenCheck=false")
public class StudentAdapterAuthTest extends BaseTest {
    @LocalServerPort
    private int port;

    @BeforeAll
    public void beforeClass() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @Nested
    @Order(1)
    class GetSingleStudentTest {
        @ParameterizedTest
        @CsvSource(value = {
                "ST000007;;guest;;401",
                "ST000007;;member;;401",
                "ST000007;;admin;;200",
                "ST000007;;;student:get;200",
                "ST000007;;other;;401",
                "ST000007;;other;student:get;200",
                "ST000007;US000002;;student:get_own;200",
                "ST000007;US000001;;student:get_own;401",
                "id_not_exist;US000001;;student:get_own;401"
        }, delimiter = ';')
        public void testGetSingleStudentWithValidToken(String id, String userId, String roles, String authorities, int expectHttpStatus) {
            given()
                    .header("Authorization", "Bearer " + buildToken(userId, roles, authorities))
                    .basePath(basePath)
                    .when()
                    .get("/students/" + id)
                    .then()
                    .statusCode(HttpStatus.valueOf(expectHttpStatus).value());
        }
    }

    @Nested
    @Order(2)
    class CreateSingleStudentTest {

        @BeforeEach
        void before() {
            reader = reader.withEndPoint("students").withMethod("post").withHttpStatus("200");
        }

        @ParameterizedTest
        @CsvSource(value = {
                ";admin;;200",
                ";;student:create;200",
                ";guest;;401",
                ";member;;401",
                ";other;;401",
                ";other;student:create;200"
        }, delimiter = ';')
        public void testGetSingleStudentWithValidToken(String userId, String roles, String authorities, int expectHttpStatus) throws IOException {
            String request = reader.withBase("requests").withFileName("create.json").getContent();

            given().log()
                    .all(true)
                    .headers(HeaderBuilder.defaultHeader()
                            .authorizationToken("Bearer " + buildToken(userId, roles, authorities))
                            .build())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .basePath(basePath)
                    .body(request)
                    .when()
                    .post("/students")
                    .then()
                    .log()
                    .all(true)
                    .assertThat()
                    .statusCode(HttpStatus.valueOf(expectHttpStatus).value());
        }
    }

    @Nested
    @Order(3)
    class UpdateSingleStudentTest {

        @BeforeEach
        void before() {
            reader = reader.withEndPoint("students").withMethod("put").withHttpStatus("200");
        }

        @ParameterizedTest
        @CsvSource(value = {
                "ST000007;;admin;;200",
                "ST000007;;;;401",
                "ST000007;;;student:update;200",
                "ST000007;US000002;;student:update;200",
                "ST000007;US000001;;student:update_own;401"
        }, delimiter = ';')
        public void testGetSingleStudentWithValidToken(String id, String userId, String roles, String authorities, int expectHttpStatus) throws IOException {
            String request = reader.withBase("requests").withFileName("update.json").getContent();

            given().log()
                    .all(true)
                    .headers(HeaderBuilder.defaultHeader()
                            .authorizationToken("Bearer " + buildToken(userId, roles, authorities))
                            .build())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .basePath(basePath)
                    .body(request)
                    .when()
                    .put("/students/" + id)
                    .then()
                    .log()
                    .all(true)
                    .assertThat()
                    .statusCode(HttpStatus.valueOf(expectHttpStatus).value());
        }
    }
}
