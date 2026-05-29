package com.xuxiaoye.api.utils;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.xuxiaoye.api.bean.CustomRequestAttribute;
import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.constant.CommonConstants;

public class ContextUtils {
    private ContextUtils() {
    }

    public static void prepareRequestContext(String appEnv, String userId, String serviceToken) {
        RequestContext reqCtx = RequestContext.builder()
                .xEnv(appEnv)
                .xLbu("business")
                .xTraceID(RandomUtils.randomString(10))
                .xContextID(RandomUtils.randomString(5))
                .authorization(serviceToken)
                .xUserId(userId)
                .build();
        CustomRequestAttribute customRequestAttribute = new CustomRequestAttribute(reqCtx);
        customRequestAttribute.setAttribute(CommonConstants.REQ_CTX_BEAN_NAME, reqCtx, RequestAttributes.SCOPE_REQUEST);
        RequestContextHolder.setRequestAttributes(customRequestAttribute);
    }
}
