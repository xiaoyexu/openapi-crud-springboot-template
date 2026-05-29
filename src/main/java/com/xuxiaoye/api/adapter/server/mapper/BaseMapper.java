package com.xuxiaoye.api.adapter.server.mapper;

import java.util.List;

import com.xuxiaoye.api.bean.PagedEntity;

public interface BaseMapper<P, PE, E> {
    P mapToPresent(E e);

    E mapToDB(P p);

    List<P> mapListToPresent(List<E> eList);

    PE mapPagedToPresent(PagedEntity<P> pPagedEntities);
}
