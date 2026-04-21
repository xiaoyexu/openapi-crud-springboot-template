package com.xuxiaoye.api.resp;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SuppressWarnings("java:S1068")
public class ResponseStatus implements Serializable {
    @JsonProperty("code")
    private String code;
    @JsonProperty("message")
    private String message;
}
