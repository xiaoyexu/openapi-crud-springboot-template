package com.xuxiaoye.api.bean;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.google.code.beanmatchers.BeanMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;

class PaginationTest {
    @Test
    void testBean() {
        MatcherAssert.assertThat(Pagination.class, allOf(
                hasValidBeanConstructor(),
                hasValidGettersAndSettersFor("offset", "limit", "sortBy"),
                hasValidBeanHashCodeFor("offset", "limit", "sortBy"),
                hasValidBeanEqualsFor("offset", "limit", "sortBy"),
                hasValidBeanToStringFor("offset", "limit", "sortBy")
        ));
    }

    @ParameterizedTest
    @CsvSource({
            "0,1,any,0,1,any",
            "1,1,any,0,1,any",
            "0,0,any,0,2147483647,any",
            "1,0,any,0,2147483647,any",
            "2,1,any,1,1,any",
            "3,1,any,2,1,any",
    })
    void testBean(Integer offset, Integer limit, String sortBy, Integer expectOffset, Integer expectLimit, String expectSortBy) {
        Pagination pagination = Pagination.of(offset, limit, sortBy);
        assertThat(pagination.getDbOffset()).isEqualTo(expectOffset);
        assertThat(pagination.getOffset()).isEqualTo(offset);
        assertThat(pagination.getDbLimit()).isEqualTo(expectLimit);
        assertThat(pagination.getLimit()).isEqualTo(limit);
        assertThat(pagination.getSortBy()).isEqualTo(expectSortBy);

        Pagination pagination2 = Pagination.of(offset, limit);
        assertThat(pagination2.getDbOffset()).isEqualTo(expectOffset);
        assertThat(pagination.getOffset()).isEqualTo(offset);
        assertThat(pagination2.getDbLimit()).isEqualTo(expectLimit);
        assertThat(pagination.getLimit()).isEqualTo(limit);
        assertThat(pagination2.getSortBy()).isEqualTo("");
    }

    @Test
    void testEmptyBean() {
        Pagination empty = Pagination.empty();
        assertThat(empty.getDbOffset()).isEqualTo(0);
        assertThat(empty.getDbLimit()).isEqualTo(Integer.MAX_VALUE);
        assertThat(empty.getSortBy()).isEqualTo("");
    }

    @Test
    void testBeanHash() {
        Pagination pagination = Pagination.of(1, 2, "abc");
        Pagination pagination2 = Pagination.of(1, 2, "abc");
        Pagination pagination3 = Pagination.of(1, 2, "xyz");
        assertThat(pagination.hashCode()).isEqualTo(pagination2.hashCode());
        assertThat(pagination.hashCode()).isNotEqualTo(pagination3.hashCode());
        assertThat(pagination.equals(pagination2)).isTrue();
        assertThat(pagination.equals(pagination3)).isFalse();

        assertThat(pagination.equals("")).isFalse();

        pagination = Pagination.of(1, null, "abc");
        pagination2 = Pagination.of(1, null, "abc");
        assertThat(pagination.hashCode()).isEqualTo(pagination2.hashCode());
        assertThat(pagination.hashCode()).isNotEqualTo(pagination3.hashCode());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "abc;true;abc",
            "+abc;true;abc",
            "-abc;false;abc",
            ",-xyz;false;xyz",
            ",-xyz;false;xyz",
            ",-xyz;false;xyz",
            " ,-xyz;false;xyz",
            " ,-xyz;false;xyz",
            " ,-xyz;false;xyz",
            "-xyz,;false;xyz",
            "-xyz,;false;xyz",
            "-xyz,;false;xyz",
            "-xyz, ;false;xyz",
            "-xyz, ;false;xyz",
            "-xyz, ;false;xyz",
    }, delimiter = ';')
    void testSortBy(String sortBy, boolean isAscending, String sortFieldName) {
        Pagination pagination = Pagination.of(0, 0, sortBy);

        SortField[] sortFields = pagination.getSortFields();
        assertThat(sortFields).hasSize(1);

        assertThat(sortFields[0].isAscending()).isEqualTo(isAscending);
        assertThat(sortFields[0].getFieldName()).isEqualTo(sortFieldName);
    }

    @ParameterizedTest
    @CsvSource(value = {
            ",",
            "''",
    })
    void testSortByEmpty(String sortBy) {
        Pagination pagination = Pagination.of(0, 0, sortBy);

        SortField[] sortFields = pagination.getSortFields();
        assertThat(sortFields).hasSize(0);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "abc,xyz;true;true;abc;xyz",
            "+abc,xyz;true;true;abc;xyz",
            "-abc,xyz;false;true;abc;xyz",
            "abc,+xyz;true;true;abc;xyz",
            "+abc,+xyz;true;true;abc;xyz",
            "-abc,+xyz;false;true;abc;xyz",
            "abc,-xyz;true;false;abc;xyz",
            "+abc,-xyz;true;false;abc;xyz",
            "-abc,-xyz;false;false;abc;xyz",
    }, delimiter = ';')
    void testSortByMultiple(String sortBy, boolean isAscending, boolean isAscending2, String sortFieldName, String sortFieldName2) {
        Pagination pagination = Pagination.of(0, 0, sortBy);

        SortField[] sortFields = pagination.getSortFields();
        assertThat(sortFields).hasSize(2);

        assertThat(sortFields[0].isAscending()).isEqualTo(isAscending);
        assertThat(sortFields[0].getFieldName()).isEqualTo(sortFieldName);
        assertThat(sortFields[1].isAscending()).isEqualTo(isAscending2);
        assertThat(sortFields[1].getFieldName()).isEqualTo(sortFieldName2);
    }
}