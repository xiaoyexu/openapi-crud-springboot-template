package com.xuxiaoye.api.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("java:S1068")
public class JWT {
    private String accessToken;
    private String refreshToken;
    private String expiresIn;
}
