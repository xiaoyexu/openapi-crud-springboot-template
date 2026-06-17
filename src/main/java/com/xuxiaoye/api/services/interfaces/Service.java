package com.xuxiaoye.api.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.FileResponse;

public interface Service<IdType, PresentDto, SearchRequest, PresentPagedEntities, PresentMapper, DBService> {
    PresentMapper getMapper();

    DBService getDBService();

    AppResponse<PresentDto> get(IdType id);

    AppResponse<PresentDto> create(PresentDto pEntity);

    AppResponse<PresentDto> updateById(IdType id, PresentDto pEntity);

    AppResponse<String> deleteById(IdType id);

    AppResponse<PresentPagedEntities> search(SearchRequest searchRequest, Pagination pagination);

    AppResponse<String> importData(MultipartFile file);

    AppResponse<FileResponse> exportData(SearchRequest searchRequest, Pagination pagination, String sheetName);
}
