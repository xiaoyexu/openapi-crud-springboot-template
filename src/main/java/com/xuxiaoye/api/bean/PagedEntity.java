package com.xuxiaoye.api.bean;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PagedEntity<T> {
    private long total;
    private List<T> data;
}
