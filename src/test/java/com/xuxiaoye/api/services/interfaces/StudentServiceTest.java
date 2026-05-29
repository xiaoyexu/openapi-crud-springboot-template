package com.xuxiaoye.api.services.interfaces;

import lombok.extern.log4j.Log4j2;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.ActiveProfiles;

import com.xuxiaoye.api.Application;
import com.xuxiaoye.api.adapter.server.mapper.StudentMapper;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.services.StudentServiceImpl;
import com.xuxiaoye.api.services.db.StudentDBService;
import com.xuxiaoye.api.services.db.dto.entity.Student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureStubRunner
@ActiveProfiles("test")
@Log4j2
class StudentServiceTest {
    @InjectMocks
    StudentServiceImpl studentService;

    @Mock
    StudentMapper studentMapper;

    @Mock
    StudentDBService studentDBService;

    @Test
    void createStudentError() {
        com.xuxiaoye.api.adapter.api.server.dto.Student student = new com.xuxiaoye.api.adapter.api.server.dto.Student();
        student.setAge(new EasyRandom().nextObject(Integer.class));
        when(studentMapper.mapToDB(student)).thenReturn(new Student());
        when(studentDBService.save(any(Student.class))).thenReturn(false);
        AppResponse<com.xuxiaoye.api.adapter.api.server.dto.Student> response = studentService.create(student);
        assertThat(response.isInternalError()).isTrue();
    }

    @Test
    void deleteStudentError() {
        Student student = new Student();
        when(studentDBService.getById(any(String.class))).thenReturn(student);
        when(studentDBService.removeById(any(String.class))).thenReturn(false);
        AppResponse<String> response = studentService.deleteById(new EasyRandom().nextObject(String.class));
        assertThat(response.isInternalError()).isTrue();
    }

    @Test
    void updateStudentError() {
        com.xuxiaoye.api.adapter.api.server.dto.Student student = new com.xuxiaoye.api.adapter.api.server.dto.Student();
        student.setAge(new EasyRandom().nextObject(Integer.class));
        when(studentMapper.mapToDB(student)).thenReturn(new Student());
        when(studentDBService.getById(any(String.class))).thenReturn(new Student());
        when(studentDBService.updateById(any(Student.class))).thenReturn(false);
        AppResponse<com.xuxiaoye.api.adapter.api.server.dto.Student> response = studentService.updateById(new EasyRandom().nextObject(String.class), student);
        assertThat(response.isInternalError()).isTrue();
    }
}
