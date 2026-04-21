package com.xuxiaoye.api.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("java:S1068")
public class SortField {
    private String fieldName;
    private boolean isAscending;
}
