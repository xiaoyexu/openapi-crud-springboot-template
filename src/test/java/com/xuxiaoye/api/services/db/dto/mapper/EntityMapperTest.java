package com.xuxiaoye.api.services.db.dto.mapper;

import com.xuxiaoye.api.services.db.dto.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class EntityMapperTest {
    @Autowired
    EntityMapper entityMapper = new EntityMapperImpl();

    @Test
    void testMapperWithNullValue() {
        assertThat(entityMapper.map((Student) null)).isNull();
    }
}