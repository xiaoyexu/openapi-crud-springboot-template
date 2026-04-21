package com.xuxiaoye.api.bean;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("java:S1068")
public class Pagination {
    private Integer offset = 0;
    private Integer limit = 20;
    private String sortBy = "";

    public Integer getDbOffset() {
        return offset == 0 ? offset : offset - 1;
    }

    public Integer getDbLimit() {
        return limit == 0 ? Integer.MAX_VALUE : limit;
    }

    public SortField[] getSortFields() {
        if (StringUtils.isEmpty(this.getSortBy())) {
            return new SortField[]{};
        }
        return Arrays.stream(this.getSortBy().split(","))
                .filter(StringUtils::isNotBlank)
                .map(sortByField -> {
                    String fieldName = "";
                    boolean isAscending = StringUtils.isNotBlank(sortByField) && !sortByField.startsWith("-");
                    if (StringUtils.isNotEmpty(sortByField)) {
                        fieldName = sortByField.startsWith("-") || sortByField.startsWith("+") ? sortByField.substring(1) : sortByField;
                    }
                    return new SortField(fieldName, isAscending);
                })
                .toArray(SortField[]::new);
    }

    public static Pagination empty() {
        return new Pagination(0, 0, "");
    }

    public static Pagination of(Integer offset, Integer limit, @NotNull String sortBy) {
        return new Pagination(offset, limit, sortBy);
    }

    public static Pagination of(Integer offset, Integer limit) {
        return new Pagination(offset, limit, "");
    }
}
