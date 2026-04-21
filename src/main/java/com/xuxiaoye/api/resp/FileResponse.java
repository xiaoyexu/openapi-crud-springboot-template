package com.xuxiaoye.api.resp;

import lombok.Data;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;

@Data
public class FileResponse {
    private MediaType contentType;
    private ContentDisposition contentDisposition;
    private String filename;
    Resource resource;
}
