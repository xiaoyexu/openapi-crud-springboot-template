package com.xuxiaoye.api.interceptors;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

import com.xuxiaoye.api.bean.RequestContext;
import com.xuxiaoye.api.constant.CommonConstants;
import com.xuxiaoye.api.constant.HeaderConstant;
import com.xuxiaoye.api.utils.LogUtils;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerInterceptor;

@Log4j2
public class RequestContextInterceptor implements HandlerInterceptor, ClientHttpRequestInterceptor {

    RequestContext requestContext;

    public RequestContextInterceptor(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Value("${server.serviceId:NOT_EXIST}")
    private String appServiceId;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String headers = Collections.list(request.getHeaderNames())
                .stream()
                .map(h -> String.format("%s=[%s]", h, request.getHeader(h)))
                .collect(Collectors.joining(" "));
        log.debug("Request headers {}", LogUtils.escapeLogMsg(headers));

        String xTraceID = request.getHeader(HeaderConstant.X_TRACE_ID);
        String xCallerContextID = request.getHeader(HeaderConstant.X_CALLER_CONTEXT_ID);
        String xContextID = request.getHeader(HeaderConstant.X_CONTEXT_ID);
        String xChatBotId = request.getHeader(HeaderConstant.X_CHATBOT_ID);
        String authorization = request.getHeader(HeaderConstant.AUTHORIZATION);
        String authorizationUser = request.getHeader(HeaderConstant.X_AUTHORIZATION_USER);
        String xLbu = request.getHeader(HeaderConstant.X_LBU);
        String xEnv = request.getHeader(HeaderConstant.X_ENV);
        String xForwardedFor = request.getHeader(HeaderConstant.X_FORWARDED_FOR);
        String acceptLanguage = request.getHeader(HeaderConstant.ACCEPT_LANGUAGE);
        String xDeviceId = request.getHeader(HeaderConstant.X_DEVICE_ID);
        String channelId = request.getHeader(HeaderConstant.CHANNEL_ID);
        String xAuthorizationApp = request.getHeader(HeaderConstant.X_AUTHORIZATION_APP);
        String serviceId = request.getHeader(HeaderConstant.SERVICE_ID);
        String userType = request.getHeader(HeaderConstant.USER_TYPE);
        String xScreenIdentifier = request.getHeader(HeaderConstant.X_SCREEN_IDENTIFIER);
        String xErrorDescription = request.getHeader(HeaderConstant.X_ERROR_DESCRIPTION);
        String xUserAccount = request.getHeader(HeaderConstant.X_USER_ACCOUNT);

        this.requestContext.setXTraceID(xTraceID);
        this.requestContext.setXContextID(xContextID);
        this.requestContext.setXChatBotID(xChatBotId);
        this.requestContext.setXCallerContextID(xCallerContextID);
        this.requestContext.setAuthorization(authorization);
        this.requestContext.setAuthorizationUser(authorizationUser);
        this.requestContext.setXLbu(xLbu);
        this.requestContext.setXEnv(xEnv);
        this.requestContext.setXForwardedFor(xForwardedFor);
        this.requestContext.setAcceptLanguage(acceptLanguage);
        this.requestContext.setXDeviceId(xDeviceId);
        this.requestContext.setChannelId(channelId);
        this.requestContext.setXAuthorizationApp(xAuthorizationApp);
        this.requestContext.setServiceId(serviceId);
        this.requestContext.setUserType(userType);
        this.requestContext.setXScreenIdentifier(xScreenIdentifier);
        this.requestContext.setXErrorDescription(xErrorDescription);
        this.requestContext.setXUserAccount(xUserAccount);

        String container = String.format(
                "xTraceID=[%s] xCallerContextID=[%s] xContextID=[%s] Authorization=[%s] xLbu =[%s] xEnv=[%s] xForwardedFor=[%s] acceptLanguage=[%s] xDeviceId=[%s] channelId=[%s] xUserId=[%s] xAuthorizationApp=[%s] serviceId=[%s] userType=[%s]",
                this.requestContext.getXTraceID(),
                this.requestContext.getXCallerContextID(),
                this.requestContext.getXContextID(),
                this.requestContext.getAuthorization(),
                this.requestContext.getXLbu(),
                this.requestContext.getXEnv(),
                this.requestContext.getXForwardedFor(),
                this.requestContext.getAcceptLanguage(),
                this.requestContext.getXDeviceId(),
                this.requestContext.getChannelId(),
                this.requestContext.getXUserId(),
                this.requestContext.getXAuthorizationApp(),
                this.requestContext.getServiceId(),
                this.requestContext.getUserType()
        );
        log.debug("Request context container initialized {}", LogUtils.escapeLogMsg(container));
        return true;
    }

    @Override
    public @Nonnull ClientHttpResponse intercept(@Nonnull HttpRequest request, @Nonnull byte[] body, @Nonnull ClientHttpRequestExecution execution) throws IOException {
        log.info("Intercept RestTemplate headers and check if [xTraceID, xContextID, xLbu, xEnv, xForwardedFor] are missing.");
        // If the below headers are missing when RestTemplate is executing HTTP method
        // Add it from requestContext
        RequestContext reqCtx = getRequestContext();

        setHeaderIfNotExists(request, HeaderConstant.X_TRACE_ID, reqCtx.getXTraceID());
        setHeaderIfNotExists(request, HeaderConstant.X_CONTEXT_ID, reqCtx.getXContextID());
        setHeaderIfNotExists(request, HeaderConstant.X_AUTHORIZATION_USER, reqCtx.getAuthorizationUser());
        setHeaderIfNotExists(request, HeaderConstant.X_AUTHORIZATION_APP, reqCtx.getXAuthorizationApp());
        setHeaderIfNotExists(request, HeaderConstant.X_LBU, reqCtx.getXLbu());
        setHeaderIfNotExists(request, HeaderConstant.X_ENV, reqCtx.getXEnv());
        setHeaderIfNotExists(request, HeaderConstant.X_FORWARDED_FOR, reqCtx.getXForwardedFor());
        setHeaderIfNotExists(request, HeaderConstant.ACCEPT_LANGUAGE, reqCtx.getAcceptLanguage());
        setHeaderIfNotExists(request, HeaderConstant.X_DEVICE_ID, reqCtx.getXDeviceId());
        setHeaderIfNotExists(request, HeaderConstant.CHANNEL_ID, reqCtx.getChannelId());
        setHeaderIfNotExists(request, HeaderConstant.SERVICE_ID, reqCtx.getServiceId());
        setHeaderIfNotExists(request, HeaderConstant.USER_TYPE, reqCtx.getUserType());
        setHeaderIfNotExists(request, HeaderConstant.X_USER_ID, reqCtx.getXUserId());
        setHeaderIfNotExists(request, HeaderConstant.X_USER_ACCOUNT, reqCtx.getXUserAccount());
        setHeaderIfNotExists(request, HeaderConstant.X_USER_EMAIL, reqCtx.getXUserEmail());

        // Invoke API
        return execution.execute(request, body);
    }

    private RequestContext getRequestContext() {
        // In case the api call is executed in a separate thread (may result in scoped bean not working - ScopeNotActiveException)
        // Load bean from RequestContextHolder
        RequestContext bean = getRequestContextFromRequestAttributes();
        if (bean != null) {
            return bean;
        }
        return this.requestContext;
    }

    private RequestContext getRequestContextFromRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        RequestContext bean = null;
        if (attributes instanceof ServletRequestAttributes attr) {
            // Normal servlet request attributes
            bean = (RequestContext) attr.getRequest().getAttribute(CommonConstants.REQ_CTX_BEAN_NAME);
        } else {
            // Customized attributes
            bean = (RequestContext) attributes.getAttribute(CommonConstants.REQ_CTX_BEAN_NAME, RequestAttributes.SCOPE_REQUEST);
        }

        return bean;
    }

    private void setHeaderIfNotExists(HttpRequest request, String header, String value) {
        if (CollectionUtils.isEmpty(request.getHeaders().get(header))) {
            request.getHeaders().add(header, value);
        }
    }
}

