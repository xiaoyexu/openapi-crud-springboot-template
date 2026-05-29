package com.xuxiaoye.api.bean;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.context.request.RequestAttributes;

import com.xuxiaoye.api.constant.CommonConstants;

public class CustomRequestAttribute implements RequestAttributes {
    private Map<String, Object> requestAttributes = new HashMap<>();

    public CustomRequestAttribute(RequestContext requestContext) {
        setAttribute(CommonConstants.REQ_CTX_BEAN_NAME, requestContext, RequestAttributes.SCOPE_REQUEST);
    }

    @Override
    public Object getAttribute(String name, int scope) {
        if (scope == RequestAttributes.SCOPE_REQUEST) {
            return this.requestAttributes.get(name);
        } else return null;
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        if (scope == RequestAttributes.SCOPE_REQUEST) {
            this.requestAttributes.put(name, value);
        }
    }

    @Override
    public void removeAttribute(String name, int scope) {
        if (scope == RequestAttributes.SCOPE_REQUEST) {
            this.requestAttributes.remove(name);
        }
    }

    @Override
    public String[] getAttributeNames(int scope) {
        if (scope == RequestAttributes.SCOPE_REQUEST) {
            return this.requestAttributes.keySet().toArray(new String[0]);
        } else return new String[0];
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback, int scope) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object resolveReference(String key) {
        return null;
    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public Object getSessionMutex() {
        return null;
    }
}
