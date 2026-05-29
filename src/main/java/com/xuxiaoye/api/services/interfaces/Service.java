package com.xuxiaoye.api.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;

public interface Service<PresentDto, PresentMapper, DBService, SearchRequest, PresentPagedEntities> {
    PresentMapper getMapper();

    DBService getDBService();

    AppResponse<PresentDto> get(String id);

    AppResponse<PresentDto> create(PresentDto pEntity);

    AppResponse<PresentDto> updateById(String id, PresentDto pEntity);

    AppResponse<String> deleteById(String id);

    AppResponse<PresentPagedEntities> search(SearchRequest searchRequest, Pagination pagination);

    AppResponse<String> importData(MultipartFile file);
}
