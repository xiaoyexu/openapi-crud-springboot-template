package com.xuxiaoye.api.resp;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppStatusTest {
    EasyRandom easyRandom = new EasyRandom();

    @Test
    void testConstructor() {
        AppStatus appStatus = new AppStatus("200", null, "");
        assertThat(appStatus.isOk()).isTrue();

        String code = easyRandom.nextObject(String.class);
        String msg = easyRandom.nextObject(String.class);

        appStatus = new AppStatus(code, null, msg);
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.getCode()).isEqualTo(code);
        assertThat(appStatus.getMessage()).isEqualTo(msg);
    }

    @Test
    void testDefaultAppStatus() {
        AppStatus appStatus = AppStatus.badRequest();
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isBadRequest()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("400");
        assertThat(appStatus.getMessage()).isEqualTo("Bad request");

        String msg = easyRandom.nextObject(String.class);
        appStatus = AppStatus.badRequest(msg);
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isBadRequest()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("400");
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        String customizedCode = easyRandom.nextObject(String.class);
        appStatus = AppStatus.badRequest(customizedCode, msg);
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isBadRequest()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("400");
        assertThat(appStatus.getCustomizedCode()).isEqualTo(customizedCode);
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        appStatus = AppStatus.unauthorized();
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isUnauthorized()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("401");
        assertThat(appStatus.getMessage()).isEqualTo("Unauthorized");

        msg = easyRandom.nextObject(String.class);
        appStatus = AppStatus.unauthorized(msg);
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isUnauthorized()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("401");
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        customizedCode = easyRandom.nextObject(String.class);
        appStatus = AppStatus.unauthorized(customizedCode, msg);
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isUnauthorized()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("401");
        assertThat(appStatus.getCustomizedCode()).isEqualTo(customizedCode);
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        appStatus = AppStatus.forbidden();
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isForbidden()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("403");
        assertThat(appStatus.getMessage()).isEqualTo("Insufficient permissions");

        msg = easyRandom.nextObject(String.class);
        appStatus = AppStatus.forbidden(msg);
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isForbidden()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("403");
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        customizedCode = easyRandom.nextObject(String.class);
        msg = easyRandom.nextObject(String.class);
        appStatus = AppStatus.forbidden(customizedCode, msg);
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isForbidden()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("403");
        assertThat(appStatus.getCustomizedCode()).isEqualTo(customizedCode);
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        appStatus = AppStatus.notFound();
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isNotFound()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("404");
        assertThat(appStatus.getMessage()).isEqualTo("Record not found");

        msg = easyRandom.nextObject(String.class);
        appStatus = AppStatus.notFound(msg);
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isNotFound()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("404");
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        customizedCode = easyRandom.nextObject(String.class);
        msg = easyRandom.nextObject(String.class);
        appStatus = AppStatus.notFound(customizedCode, msg);
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isNotFound()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("404");
        assertThat(appStatus.getCustomizedCode()).isEqualTo(customizedCode);
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        appStatus = AppStatus.internalError();
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isInternalError()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("500");
        assertThat(appStatus.getMessage()).isEqualTo("Internal Server Error");

        msg = easyRandom.nextObject(String.class);
        appStatus = AppStatus.internalError(msg);
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isInternalError()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("500");
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        customizedCode = easyRandom.nextObject(String.class);
        msg = easyRandom.nextObject(String.class);
        appStatus = AppStatus.internalError(customizedCode, msg);
        assertThat(appStatus.isOk()).isFalse();
        assertThat(appStatus.isInternalError()).isTrue();
        assertThat(appStatus.getCode()).isEqualTo("500");
        assertThat(appStatus.getCustomizedCode()).isEqualTo(customizedCode);
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        appStatus = AppStatus.fromHttpStatusCode(200);
        assertThat(appStatus.isOk()).isTrue();

        msg = easyRandom.nextObject(String.class);
        appStatus = AppStatus.fromHttpStatusCode(200, msg);
        assertThat(appStatus.isOk()).isTrue();
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        appStatus = AppStatus.fromHttpStatusCode(400);
        assertThat(appStatus.isBadRequest()).isTrue();

        msg = easyRandom.nextObject(String.class);
        appStatus = AppStatus.fromHttpStatusCode(400, msg);
        assertThat(appStatus.isBadRequest()).isTrue();
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        appStatus = AppStatus.fromHttpStatusCode(401);
        assertThat(appStatus.isUnauthorized()).isTrue();

        msg = easyRandom.nextObject(String.class);
        appStatus = AppStatus.fromHttpStatusCode(401, msg);
        assertThat(appStatus.isUnauthorized()).isTrue();
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        appStatus = AppStatus.fromHttpStatusCode(404);
        assertThat(appStatus.isNotFound()).isTrue();

        msg = easyRandom.nextObject(String.class);
        appStatus = AppStatus.fromHttpStatusCode(404, msg);
        assertThat(appStatus.isNotFound()).isTrue();
        assertThat(appStatus.getMessage()).isEqualTo(msg);

        appStatus = AppStatus.fromHttpStatusCode(500);
        assertThat(appStatus.isInternalError()).isTrue();

        msg = easyRandom.nextObject(String.class);
        appStatus = AppStatus.fromHttpStatusCode(500, msg);
        assertThat(appStatus.isInternalError()).isTrue();
        assertThat(appStatus.getMessage()).isEqualTo(msg);
    }

    @Test
    void testEquals() {
        EqualsVerifier.simple().forClass(AppStatus.class).verify();
    }
}