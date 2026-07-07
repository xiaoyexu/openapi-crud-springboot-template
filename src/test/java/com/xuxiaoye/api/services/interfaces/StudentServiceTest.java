package com.xuxiaoye.api.services.interfaces;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xuxiaoye.api.adapter.server.mapper.StudentMapper;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.services.StudentServiceImpl;
import com.xuxiaoye.api.services.db.StudentDBService;
import com.xuxiaoye.api.services.db.dto.entity.Student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
        AppResponse<com.xuxiaoye.api.adapter.api.server.dto.Student> response = studentService.updateById(new EasyRandom().nextObject(String.class), student);
        assertThat(response.isInternalError()).isTrue();
    }
}
