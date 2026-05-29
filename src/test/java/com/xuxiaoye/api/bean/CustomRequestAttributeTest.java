package com.xuxiaoye.api.bean;

import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestAttributes;

import com.xuxiaoye.api.constant.CommonConstants;

import static org.assertj.core.api.Assertions.assertThat;

class CustomRequestAttributeTest {
    @Test
    void testCustomRequestAttribute() {
        RequestContext reqCtx = RequestContext.builder().xEnv("dev").build();
        CustomRequestAttribute attribute = new CustomRequestAttribute(reqCtx);

        RequestContext result = (RequestContext) attribute.getAttribute(CommonConstants.REQ_CTX_BEAN_NAME, RequestAttributes.SCOPE_REQUEST);
        assertThat(result).isEqualTo(reqCtx);

        result = (RequestContext) attribute.getAttribute("not exist", RequestAttributes.SCOPE_REQUEST);
        assertThat(result).isNull();

        RequestContext reqCtx2 = RequestContext.builder()
                .xEnv("dev2")
                .build();
        attribute.setAttribute(CommonConstants.REQ_CTX_BEAN_NAME, reqCtx2, RequestAttributes.SCOPE_REQUEST);
        result = (RequestContext) attribute.getAttribute(CommonConstants.REQ_CTX_BEAN_NAME, RequestAttributes.SCOPE_REQUEST);
        assertThat(result).isEqualTo(reqCtx2);

        attribute.setAttribute(CommonConstants.REQ_CTX_BEAN_NAME, reqCtx2, RequestAttributes.SCOPE_SESSION);
        result = (RequestContext) attribute.getAttribute(CommonConstants.REQ_CTX_BEAN_NAME, RequestAttributes.SCOPE_SESSION);
        assertThat(result).isNull();

        String[] names = attribute.getAttributeNames(RequestAttributes.SCOPE_REQUEST);
        assertThat(names).hasSize(1);

        names = attribute.getAttributeNames(RequestAttributes.SCOPE_SESSION);
        assertThat(names).hasSize(0);

        attribute.removeAttribute(CommonConstants.REQ_CTX_BEAN_NAME, RequestAttributes.SCOPE_SESSION);
        result = (RequestContext) attribute.getAttribute(CommonConstants.REQ_CTX_BEAN_NAME, RequestAttributes.SCOPE_REQUEST);
        assertThat(result).isEqualTo(reqCtx2);

        attribute.removeAttribute(CommonConstants.REQ_CTX_BEAN_NAME, RequestAttributes.SCOPE_REQUEST);
        result = (RequestContext) attribute.getAttribute(CommonConstants.REQ_CTX_BEAN_NAME, RequestAttributes.SCOPE_REQUEST);
        assertThat(result).isNull();

        assertThat(attribute.resolveReference("")).isNull();
        assertThat(attribute.getSessionId()).isNull();
        assertThat(attribute.getSessionMutex()).isNull();
    }
}