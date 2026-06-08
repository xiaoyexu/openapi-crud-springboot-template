package com.xuxiaoye.api.adapter.server;

import java.io.File;
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
import org.springframework.test.context.ActiveProfiles;

import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.BaseTest;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(10)
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
    class SearchUserAuditTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("user-audits/search").withMethod("post");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource(value = {
                    // filter
                    "search.json;search_result.json;2;0;",
                    "search_ids.json;search_ids_result.json;2;0;",
                    // "search_createdAts_bt.json;search_createdAts_bt_result.json;2;0;",
                    // "search_createdAts_ge.json;search_createdAts_ge_result.json;2;0;",
                    // "search_createdAts_le.json;search_createdAts_le_result.json;2;0;",
                    // "search_updatedAts_bt.json;search_updatedAts_bt_result.json;2;0;",
                    // "search_updatedAts_ge.json;search_updatedAts_ge_result.json;2;0;",
                    // "search_updatedAts_le.json;search_updatedAts_le_result.json;2;0;",
                    // "search_createdBys_system.json;search_createdBys_system_result.json;2;0;",
                    // "search_createdBys_user.json;search_createdBys_user_result.json;2;0;",
                    // "search_updatedBys_system.json;search_updatedBys_system_result.json;2;0;",
                    // "search_updatedBys_user.json;search_updatedBys_user_result.json;2;0;",
                    // pagination
                    // "search.json;search_result_2_1.json;2,1;",
                    // "search.json;search_result_2_2.json;2,2;",
                    // "search.json;search_result_2_3.json;2,3;",
                    // "search_multiple.json;search_multiple_result.json;2;0;",
                    // sort
                    "search.json;search_result_id_asc.json;2;0;id;",
                    "search.json;search_result_id_desc.json;2;0;-id;",
                    // use ';' as ',' will be used for multiple column sorting
                    // "search.json;search_result_c1_c2_c3_asc.json;2;0;column1,column2,column3;",
            }, delimiter = ';')
            void searchUserAudit(String requestJson, String responseJson, Integer limit, Integer offset, String sortBy) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = post(
                        "/user-audits/search",
                        request,
                        HttpStatus.OK.value(),
                        "limit", limit,
                        "offset", offset,
                        "sortBy", sortBy
                );

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(2)
    class GetSingleUserAuditTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("user-audits").withMethod("get");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "1,get_result.json",
            })
            void getSingleUserAudit(String id, String responseJson) throws IOException {

                String jsonResponse = get("/user-audits/" + id, HttpStatus.OK.value());

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }

        @Nested
        class Code404 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("404");
            }

            @ParameterizedTest
            @CsvSource({
                    "100000,get_result.json",
            })
            void getSingleUserAudit(String id, String responseJson) throws IOException {

                String jsonResponse = get("/user-audits/" + id, HttpStatus.NOT_FOUND.value());

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(3)
    class ExportUserAuditsTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("user-audits/search").withMethod("post");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource(value = {
                    // filter
                    "search.json;2;0;",
            }, delimiter = ';')
            void exportUserAudit(String requestJson, Integer limit, Integer offset, String sortBy) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                postAcceptAny(
                        "/user-audits/export",
                        request,
                        HttpStatus.OK.value(),
                        "limit", limit,
                        "offset", offset,
                        "sortBy", sortBy
                );
            }
        }
    }

    @Nested
    @Order(10)
    class SearchUserTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("users/search").withMethod("post");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource(value = {
                    // filter
                    "search.json;search_result.json;2;0;",
                    "search_createdAts_bt.json;search_createdAts_bt_result.json;2;0;",
                    "search_createdAts_ge.json;search_createdAts_ge_result.json;2;0;",
                    "search_createdAts_le.json;search_createdAts_le_result.json;2;0;",
                    "search_updatedAts_bt.json;search_updatedAts_bt_result.json;2;0;",
                    "search_updatedAts_ge.json;search_updatedAts_ge_result.json;2;0;",
                    "search_updatedAts_le.json;search_updatedAts_le_result.json;2;0;",
                    "search_createdBys_system.json;search_createdBys_system_result.json;2;0;",
                    "search_createdBys_user.json;search_createdBys_user_result.json;2;0;",
                    "search_updatedBys_system.json;search_updatedBys_system_result.json;2;0;",
                    "search_updatedBys_user.json;search_updatedBys_user_result.json;2;0;",
                    // pagination
                    // "search.json;search_result_2_1.json;2,1;",
                    // "search.json;search_result_2_2.json;2,2;",
                    // "search.json;search_result_2_3.json;2,3;",
                    // "search_multiple.json;search_multiple_result.json;2;0;",
                    // sort
                    "search.json;search_result_id_asc.json;2;0;id;",
                    "search.json;search_result_id_desc.json;2;0;-id;",
                    // use ';' as ',' will be used for multiple column sorting
                    // "search.json;search_result_c1_c2_c3_asc.json;2;0;column1,column2,column3;",
            }, delimiter = ';')
            void searchUser(String requestJson, String responseJson, Integer limit, Integer offset, String sortBy) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = post(
                        "/users/search",
                        request,
                        HttpStatus.OK.value(),
                        "limit", limit,
                        "offset", offset,
                        "sortBy", sortBy
                );

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(20)
    class GetSingleUserTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("users").withMethod("get");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "US000001,get_result.json",
            })
            void getSingleUser(String id, String responseJson) throws IOException {

                String jsonResponse = get("/users/" + id, HttpStatus.OK.value());

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }

        @Nested
        class Code404 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("404");
            }

            @ParameterizedTest
            @CsvSource({
                    "no_such_id,get_result.json",
            })
            void getSingleUser(String id, String responseJson) throws IOException {

                String jsonResponse = get("/users/" + id, HttpStatus.NOT_FOUND.value());

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(30)
    class UpdateUserTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("users").withMethod("put");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "update.json,US000001,update_result.json",
            })
            void updateUser(String requestJson, String id, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = put(
                        "/users/" + id,
                        request,
                        HttpStatus.OK.value()
                );

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse)
                        .whenIgnoringPaths("data.updatedAt")
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
                    "update.json,id,update_result.json",
            })
            void updateUser(String requestJson, String id, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = put(
                        "/users/" + id,
                        request,
                        HttpStatus.BAD_REQUEST.value()
                );

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }

        @Nested
        class Code404 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("404");
            }

            @ParameterizedTest
            @CsvSource({
                    "update.json,id,update_result.json",
            })
            void updateUser(String requestJson, String id, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = put(
                        "/users/" + id,
                        request,
                        HttpStatus.NOT_FOUND.value()
                );

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(40)
    class DeleteUserTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("users").withMethod("delete");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "US000001,delete_result.json",
            })
            void deleteUser(String id, String responseJson) throws IOException {

                String jsonResponse = delete("/users/" + id, HttpStatus.OK.value());

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);

                // Expect Not Found
                delete("/users/" + id, HttpStatus.NOT_FOUND.value());
            }
        }

        @Nested
        class Code404 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("404");
            }

            @ParameterizedTest
            @CsvSource({
                    "id,delete_result.json",
            })
            void deleteUser(String id, String responseJson) throws IOException {
                String jsonResponse = delete("/users/" + id, HttpStatus.NOT_FOUND.value());

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(50)
    class CreateUserTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("users").withMethod("post");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "create.json,create_result.json",
            })
            void createUser(String requestJson, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = post(
                        "/users",
                        request,
                        HttpStatus.OK.value()
                );

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse)
                        .whenIgnoringPaths("data.id", "data.createdAt", "data.updatedAt")
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
                    "create.json,create_result.json",
            })
            void createUser(String requestJson, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = post(
                        "/users",
                        request,
                        HttpStatus.BAD_REQUEST.value()
                );

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(60)
    class ExportUsersTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("users/search").withMethod("post");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource(value = {
                    // filter
                    "search.json;2;0;",
            }, delimiter = ';')
            void exportUser(String requestJson, Integer limit, Integer offset, String sortBy) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                postAcceptAny(
                        "/users/export",
                        request,
                        HttpStatus.OK.value(),
                        "limit", limit,
                        "offset", offset,
                        "sortBy", sortBy
                );
            }
        }
    }

    @Nested
    @Order(70)
    class ImportUsersTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("users/import").withMethod("post");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "Users_for_import.xlsx,ok.json",
            })
            void importUsers(String requestFile, String responseJson) throws IOException {
                File file = reader.withBase("requests").withFileName(requestFile).getResource().getFile();

                String jsonResponse = postFile(
                        "/users/import",
                        "file",
                        file,
                        HttpStatus.OK.value()
                );

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }
}