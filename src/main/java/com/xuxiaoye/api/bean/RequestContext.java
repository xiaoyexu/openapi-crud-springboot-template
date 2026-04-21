package com.xuxiaoye.api.bean;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@Builder
public class RequestContext {

    String xTraceID;
    String xCallerContextID;
    String xContextID;
    String xChatBotID;
    String authorization;
    String authorizationUser;
    String xLbu;
    String xEnv;
    String xForwardedFor;
    String acceptLanguage;
    String xDeviceId;
    String channelId;
    String xAuthorizationApp;
    String serviceId;
    String staffId;
    String userType;
    String xScreenIdentifier;
    String xErrorDescription;

    String xUserId; // refer to db id
    String xUserAccount; // account name, current refer to user email
    String xUserEmail; // refer to user email
}
