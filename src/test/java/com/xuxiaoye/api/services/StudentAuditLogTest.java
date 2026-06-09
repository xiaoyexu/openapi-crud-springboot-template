package com.xuxiaoye.api.services;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.log4j.Log4j2;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.ActiveProfiles;

import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.services.db.StudentAuditDBService;
import com.xuxiaoye.api.services.db.StudentDBService;
import com.xuxiaoye.api.services.db.dto.entity.Student;
import com.xuxiaoye.api.services.db.dto.entity.StudentAudit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static com.xuxiaoye.api.interceptors.TableAuditLogInterceptor.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
public class StudentAuditLogTest {
    @Autowired
    StudentDBService studentDBService;

    @Autowired
    StudentAuditDBService studentAuditDBService;

    EasyRandom easyRandom = new EasyRandom();

    Student randomStudent() {
        return easyRandom.nextObject(Student.class);
    }

    @Nested
    class Create {
        @Test
        void createOne() {
            Student student = randomStudent();

            assertDoesNotThrow(() -> studentDBService.saveOrUpdate(student));

            List<StudentAudit> audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
        }

        @Test
        void saveOne() {
            Student student = randomStudent();

            assertDoesNotThrow(() -> studentDBService.save(student));

            List<StudentAudit> audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
        }
    }

    @Nested
    class Update {
        @Test
        void updateOne() {
            Student student = randomStudent();

            assertDoesNotThrow(() -> studentDBService.saveOrUpdate(student));

            List<StudentAudit> audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            // Update
            String updatedValue = easyRandom.nextObject(String.class);
            student.setName(updatedValue);

            assertDoesNotThrow(() -> studentDBService.saveOrUpdate(student));

            audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.UPDATE.getAction());
            assertThat(audits.get(1).getName()).isEqualTo(updatedValue);
        }

        @Test
        void updateByListWithOne() {
            Student student = randomStudent();

            assertDoesNotThrow(() -> studentDBService.saveOrUpdate(student));

            List<StudentAudit> audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            // Update
            String updatedValue = easyRandom.nextObject(String.class);
            student.setName(updatedValue);

            assertDoesNotThrow(() -> studentDBService.updateBatchById(List.of(student)));

            audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.UPDATE.getAction());
            assertThat(audits.get(1).getName()).isEqualTo(updatedValue);
        }

        @Test
        void updateByListWithTwo() {
            Student student = randomStudent();

            assertDoesNotThrow(() -> studentDBService.saveOrUpdate(student));

            List<StudentAudit> audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            Student student2 = randomStudent();

            assertDoesNotThrow(() -> studentDBService.saveOrUpdate(student2));

            List<StudentAudit> audits2 = studentAuditDBService.listAuditsByDataPkId(student2.getId());
            assertThat(audits2).hasSize(1);
            assertThat(audits2.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            // Update
            String updatedValue = easyRandom.nextObject(String.class);
            student.setName(updatedValue);

            // Update
            String updatedValue2 = easyRandom.nextObject(String.class);
            student2.setName(updatedValue2);

            assertDoesNotThrow(() -> studentDBService.updateBatchById(List.of(student, student2)));

            audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.UPDATE.getAction());
            assertThat(audits.get(1).getName()).isEqualTo(updatedValue);

            audits2 = studentAuditDBService.listAuditsByDataPkId(student2.getId());
            assertThat(audits2).hasSize(2);
            assertThat(audits2.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits2.get(1).getAction()).isEqualTo(AuditAction.UPDATE.getAction());
            assertThat(audits2.get(1).getName()).isEqualTo(updatedValue2);
        }

        @Test
        void updateByWrapper() {
            Student student = randomStudent();

            assertDoesNotThrow(() -> studentDBService.saveOrUpdate(student));

            List<StudentAudit> audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            // Update
            String updatedValue = easyRandom.nextObject(String.class);

            assertDoesNotThrow(() -> studentDBService.update(
                    new LambdaUpdateWrapper<Student>().eq(Student::getName, student.getName()).set(Student::getName, updatedValue))
            );

            audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.UPDATE.getAction());
            assertThat(audits.get(1).getName()).isEqualTo(updatedValue);
        }

        @Test
        void updatePKValue() {
            Student student = randomStudent();

            assertDoesNotThrow(() -> studentDBService.saveOrUpdate(student));

            List<StudentAudit> audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            // Update
            String updatedValue = easyRandom.nextObject(String.class);

            assertDoesNotThrow(() -> studentDBService.update(
                    new LambdaUpdateWrapper<Student>().eq(Student::getId, student.getId()).set(Student::getId, updatedValue))
            );

            audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.DELETE.getAction());
            assertThat(audits.get(1).getId()).isEqualTo(student.getId());
        }
    }

    @Nested
    class Delete {
        @Test
        void deleteByEntity() {
            Student student = randomStudent();

            assertDoesNotThrow(() -> studentDBService.saveOrUpdate(student));

            List<StudentAudit> audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            assertDoesNotThrow(() -> studentDBService.removeById(student));

            audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.DELETE.getAction());
        }

        @Test
        void deleteById() {
            Student student = randomStudent();

            assertDoesNotThrow(() -> studentDBService.saveOrUpdate(student));

            List<StudentAudit> audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());

            assertDoesNotThrow(() -> studentDBService.removeById(student.getId()));

            audits = studentAuditDBService.listAuditsByDataPkId(student.getId());
            assertThat(audits).hasSize(2);
            assertThat(audits.get(0).getAction()).isEqualTo(AuditAction.CREATE.getAction());
            assertThat(audits.get(1).getAction()).isEqualTo(AuditAction.DELETE.getAction());
        }
    }
}