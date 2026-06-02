package com.xuxiaoye.api.adapter.server;

import java.io.File;
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

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class RoleAdapterTest extends BaseTest {

    @LocalServerPort
    private int port;

    @BeforeAll
    public void beforeClass(TestInfo info) {
        RestAssured.baseURI = "http://localhost:" + port;
        log.info("Starting test case {}", info.getDisplayName());
    }

    @Nested
    @Order(1)
    class SearchRoleAuditTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("role-audits/search").withMethod("post");
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
                    // "search_createdAt_bt.json;search_createdAt_bt_result.json;2;0;",
                    // "search_createdAt_ge.json;search_createdAt_ge_result.json;2;0;",
                    // "search_createdAt_le.json;search_createdAt_le_result.json;2;0;",
                    // "search_updatedAt_bt.json;search_updatedAt_bt_result.json;2;0;",
                    // "search_updatedAt_ge.json;search_updatedAt_ge_result.json;2;0;",
                    // "search_updatedAt_le.json;search_updatedAt_le_result.json;2;0;",
                    // "search_createdBy_system.json;search_createdBy_system_result.json;2;0;",
                    // "search_createdBy_user.json;search_createdBy_user_result.json;2;0;",
                    // "search_updatedBy_system.json;search_updatedBy_system_result.json;2;0;",
                    // "search_updatedBy_user.json;search_updatedBy_user_result.json;2;0;",
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
            void searchRoleAudit(String requestJson, String responseJson, Integer limit, Integer offset, String sortBy) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = post(
                        "/role-audits/search",
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
    class GetSingleRoleAuditTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("role-audits").withMethod("get");
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
            void getSingleRoleAudit(String id, String responseJson) throws IOException {

                String jsonResponse = get("/role-audits/" + id, HttpStatus.OK.value());

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
            void getSingleRoleAudit(String id, String responseJson) throws IOException {

                String jsonResponse = get("/role-audits/" + id, HttpStatus.NOT_FOUND.value());

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(3)
    class ExportRoleAuditsTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("role-audits/search").withMethod("post");
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
            void exportRoleAudit(String requestJson, Integer limit, Integer offset, String sortBy) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                postAcceptAny(
                        "/role-audits/export",
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
    class SearchRoleTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("roles/search").withMethod("post");
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
                    "search_keyword.json;search_keyword_result.json;2;0;",
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
            void searchRole(String requestJson, String responseJson, Integer limit, Integer offset, String sortBy) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = post(
                        "/roles/search",
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
    class GetSingleRoleTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("roles").withMethod("get");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "RO000001,get_result.json",
            })
            void getSingleRole(String id, String responseJson) throws IOException {

                String jsonResponse = get("/roles/" + id, HttpStatus.OK.value());

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(30)
    class UpdateRoleTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("roles").withMethod("put");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "update.json,RO000001,update_result.json",
            })
            void updateRole(String requestJson, String id, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = put("/roles/" + id, request, HttpStatus.OK.value());

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
            void updateRole(String requestJson, String id, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = put("/roles/" + id, request, HttpStatus.BAD_REQUEST.value());

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
            void updateRole(String requestJson, String id, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = put("/roles/" + id, request, HttpStatus.NOT_FOUND.value());

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(40)
    class DeleteRoleTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("roles").withMethod("delete");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "RO000001,delete_result.json",
            })
            void deleteRole(String id, String responseJson) throws IOException {

                String jsonResponse = delete("/roles/" + id, HttpStatus.OK.value());

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);

                // Expect Not Found
                get("/roles/" + id, HttpStatus.NOT_FOUND.value());
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
            void deleteRole(String id, String responseJson) throws IOException {

                String jsonResponse = delete("/roles/" + id, HttpStatus.NOT_FOUND.value());

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(50)
    class CreateRoleTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("roles").withMethod("post");
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
            void createRole(String requestJson, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = post(
                        "/roles",
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
            void createRole(String requestJson, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = post(
                        "/roles",
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
    class ExportRolesTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("roles/search").withMethod("post");
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
            void exportRole(String requestJson, Integer limit, Integer offset, String sortBy) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                postAcceptAny(
                        "/roles/export",
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
    class ImportRolesTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("roles/import").withMethod("post");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "Roles_for_import.xlsx,ok.json",
            })
            void exportRoles(String requestFile, String responseJson) throws IOException {
                File file = reader.withBase("requests").withFileName(requestFile).getResource().getFile();

                String jsonResponse = postFile("/roles/import", "file", file, HttpStatus.OK.value());

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }
}
