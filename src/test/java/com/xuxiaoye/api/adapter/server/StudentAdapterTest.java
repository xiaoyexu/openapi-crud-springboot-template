
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

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
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@Order(2)
class StudentAdapterTest extends BaseTest {

    @LocalServerPort
    private int port;

    @BeforeAll
    public void beforeClass(TestInfo info) {
        RestAssured.baseURI = "http://localhost:" + port;
        log.info("Starting test case {}", info.getDisplayName());
    }

    @Nested
    @Order(1)
    class SearchStudentAuditTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("student-audits/search").withMethod("post");
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
                    "search_createdAt_bt.json;search_createdAt_bt_result.json;2;0;",
                    "search_createdAt_ge.json;search_createdAt_ge_result.json;2;0;",
                    "search_createdAt_le.json;search_createdAt_le_result.json;2;0;",
                    "search_updatedAt_bt.json;search_updatedAt_bt_result.json;2;0;",
                    "search_updatedAt_ge.json;search_updatedAt_ge_result.json;2;0;",
                    "search_updatedAt_le.json;search_updatedAt_le_result.json;2;0;",
                    "search_createdBy_system.json;search_createdBy_system_result.json;2;0;",
                    "search_createdBy_user.json;search_createdBy_user_result.json;2;0;",
                    "search_updatedBy_system.json;search_updatedBy_system_result.json;2;0;",
                    "search_updatedBy_user.json;search_updatedBy_user_result.json;2;0;",
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
            void searchStudentAudit(String requestJson, String responseJson, Integer limit, Integer offset, String sortBy) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .queryParams(
                                "limit", limit,
                                "offset", offset,
                                "sortBy", sortBy
                        )
                        .basePath(basePath)
                        .body(request)
                        .when()
                        .post("/student-audits/search")
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(2)
    class GetSingleStudentAuditTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("student-audits").withMethod("get");
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
            void getSingleStudentAudit(String id, String responseJson) throws IOException {

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .basePath(basePath)
                        .when()
                        .get("/student-audits/" + id)
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();

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
            void getSingleStudentAudit(String id, String responseJson) throws IOException {

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .basePath(basePath)
                        .when()
                        .get("/student-audits/" + id)
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(3)
    class ExportStudentAuditsTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("student-audits/search").withMethod("post");
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
            void exportStudentAudit(String requestJson, Integer limit, Integer offset, String sortBy) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .queryParams(
                                "limit", limit,
                                "offset", offset,
                                "sortBy", sortBy
                        )
                        .basePath(basePath)
                        .body(request)
                        .when()
                        .post("/student-audits/export")
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();
            }
        }
    }

    @Nested
    @Order(5)
    class ListStudentTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("students").withMethod("get");
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
                    "list_result.json;2;0;",
                    // pagination
                    // "search.json;search_result_2_1.json;2,1;",
                    // "search.json;search_result_2_2.json;2,2;",
                    // "search.json;search_result_2_3.json;2,3;",
                    // "search_multiple.json;search_multiple_result.json;2;0;",
                    // sort
                    "list_result_id_asc.json;2;0;id;",
                    "list_result_id_desc.json;2;0;-id;",
            }, delimiter = ';')
            void listStudent(String responseJson, Integer limit, Integer offset, String sortBy) throws IOException {

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .queryParams(
                                "limit", limit,
                                "offset", offset,
                                "sortBy", sortBy
                        )
                        .basePath(basePath)
                        .when()
                        .get("/students")
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(10)
    class SearchStudentTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("students/search").withMethod("post");
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
                    "search_keyword1.json;search_keyword1_result.json;2;0;",
                    "search_ids.json;search_ids_result.json;2;0;",
                    "search_names.json;search_names_result.json;2;0;",
                    "search_ages.json;search_ages_result.json;2;0;",
                    "search_heights.json;search_heights_result.json;2;0;",
                    "search_birthdays.json;search_birthdays_result.json;2;0;",
                    "search_createdAt_bt.json;search_createdAt_bt_result.json;2;0;",
                    "search_createdAt_ge.json;search_createdAt_ge_result.json;2;0;",
                    "search_createdAt_le.json;search_createdAt_le_result.json;2;0;",
                    "search_updatedAt_bt.json;search_updatedAt_bt_result.json;2;0;",
                    "search_updatedAt_ge.json;search_updatedAt_ge_result.json;2;0;",
                    "search_updatedAt_le.json;search_updatedAt_le_result.json;2;0;",
                    "search_createdBy_system.json;search_createdBy_system_result.json;2;0;",
                    "search_createdBy_user.json;search_createdBy_user_result.json;2;0;",
                    "search_updatedBy_system.json;search_updatedBy_system_result.json;2;0;",
                    "search_updatedBy_user.json;search_updatedBy_user_result.json;2;0;",
                    // pagination
                    // "search.json;search_result_2_1.json;2,1;",
                    // "search.json;search_result_2_2.json;2,2;",
                    // "search.json;search_result_2_3.json;2,3;",
                    // "search_multiple.json;search_multiple_result.json;2;0;",
                    // sort
                    "search.json;search_result_id_asc.json;2;0;id;",
                    "search.json;search_result_id_desc.json;2;0;-id;",
            }, delimiter = ';')
            void searchStudent(String requestJson, String responseJson, Integer limit, Integer offset, String sortBy) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .queryParams(
                                "limit", limit,
                                "offset", offset,
                                "sortBy", sortBy
                        )
                        .basePath(basePath)
                        .body(request)
                        .when()
                        .post("/students/search")
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }

        }
    }

    @Nested
    @Order(20)
    class GetSingleStudentTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("students").withMethod("get");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "ST000001,get_result.json",
            })
            void getSingleStudent(String id, String responseJson) throws IOException {

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .basePath(basePath)
                        .when()
                        .get("/students/" + id)
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();

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
            void getSingleStudent(String id, String responseJson) throws IOException {

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .basePath(basePath)
                        .when()
                        .get("/students/" + id)
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }

        }
    }

    @Nested
    @Order(30)
    class UpdateStudentTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("students").withMethod("put");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "update.json,ST000001,update_result.json",
            })
            void updateStudent(String requestJson, String id, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
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
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();

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
            void updateStudent(String requestJson, String id, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
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
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .extract()
                        .asString();

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
            void updateStudent(String requestJson, String id, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
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
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(40)
    class DeleteStudentTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("students").withMethod("delete");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "ST000001,delete_result.json",
            })
            void deleteStudent(String id, String responseJson) throws IOException {

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .basePath(basePath)
                        .when()
                        .delete("/students/" + id)
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);

                // Expect Not Found
                given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .basePath(basePath)
                        .when()
                        .get("/students/" + id)
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .extract()
                        .asString();
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
            void deleteStudent(String id, String responseJson) throws IOException {
                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .basePath(basePath)
                        .when()
                        .delete("/students/" + id)
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(50)
    class CreateStudentTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("students").withMethod("post");
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
            void createStudent(String requestJson, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader()
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
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();

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
            void createStudent(String requestJson, String responseJson) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader()
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
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }

    @Nested
    @Order(60)
    class ExportStudentsTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("students/search").withMethod("post");
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
            void exportStudent(String requestJson, Integer limit, Integer offset, String sortBy) throws IOException {
                String request = reader.withBase("requests").withFileName(requestJson).getContent();

                given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .queryParams(
                                "limit", limit,
                                "offset", offset,
                                "sortBy", sortBy
                        )
                        .basePath(basePath)
                        .body(request)
                        .when()
                        .post("/students/export")
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();
            }
        }
    }

    @Nested
    @Order(70)
    class ImportStudentsTest {
        @BeforeEach
        void before() {
            reader = reader.withEndPoint("students/import").withMethod("post");
        }

        @Nested
        class Code200 {

            @BeforeEach
            void before() {
                reader = reader.withHttpStatus("200");
            }

            @ParameterizedTest
            @CsvSource({
                    "Students_for_import.xlsx,ok.json",
            })
            void importStudent(String requestFile, String responseJson) throws IOException {
                File file = reader.withBase("requests").withFileName(requestFile).getResource().getFile();

                String jsonResponse = given().log()
                        .all(true)
                        .headers(HeaderBuilder.defaultHeader().build())
                        .basePath(basePath)
                        .multiPart("file", file)
                        .when()
                        .post("/students/import")
                        .then()
                        .log()
                        .all(true)
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract()
                        .asString();

                String mockRes = reader.withBase("responses").withFileName(responseJson).getContent();
                assertThatJson(jsonResponse).isEqualTo(mockRes);
            }
        }
    }
}
