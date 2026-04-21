package com.xuxiaoye.api.client;

import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClientResponseException;

import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.resp.ErrorResponse;
import com.xuxiaoye.api.resp.ResponseStatus;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BaseApiClientTest {
    EasyRandom easyRandom = new EasyRandom();

    @Test
    void testHandleApiCall() {
        String data = easyRandom.nextObject(String.class);

        BaseApiClient baseApiClient = new BaseApiClient();
        AppResponse<Object> appResponse = baseApiClient.handleApiCall(() -> AppResponse.okWithData(data));
        assertThat(appResponse).isNotNull();
        assertThat(appResponse.getData()).isEqualTo(data);
        assertThat(appResponse.getStatus().getCode()).isEqualTo("200");
        assertThat(appResponse.getStatus().getMessage()).isEqualTo("ok");
    }

    @Test
    void testHandleApiCallWithRuntimeException() {
        String errorMsg = easyRandom.nextObject(String.class);

        BaseApiClient baseApiClient = new BaseApiClient();
        AppResponse<Object> appResponse = baseApiClient.handleApiCall(() -> {
            throw new RuntimeException(errorMsg);
        });
        assertThat(appResponse).isNotNull();
        assertThat(appResponse.getStatus().getCode()).isEqualTo("500");
        assertThat(appResponse.getStatus().getMessage()).isEqualTo(errorMsg);
    }

    @Test
    void testHandleApiCallWithRuntimeExceptionAndHandler() {
        String errorMsg = easyRandom.nextObject(String.class);

        BaseApiClient baseApiClient = new BaseApiClient();
        AppResponse<Object> appResponse = baseApiClient.handleApiCall(() -> {
            throw new RuntimeException(errorMsg);
        }, exp -> AppResponse.failWithStatus(AppStatus.internalError()));
        assertThat(appResponse).isNotNull();
        assertThat(appResponse.getStatus().getCode()).isEqualTo("500");
        assertThat(appResponse.getStatus().getMessage()).isEqualTo(errorMsg);

        appResponse = baseApiClient.handleApiCall(() -> {
            throw new RuntimeException(errorMsg);
        }, exp -> AppResponse.okWithData("ok"));
        assertThat(appResponse).isNotNull();
        assertThat(appResponse.getStatus().getCode()).isEqualTo("500");
        assertThat(appResponse.getStatus().getMessage()).isEqualTo(errorMsg);
    }

    @Test
    void testHandleApiCallWithRestClientException() {
        String errorMsg = easyRandom.nextObject(String.class);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(ResponseStatus.builder().code("400").message(errorMsg).build());

        RestClientResponseException exception = mock(RestClientResponseException.class);

        when(exception.getStatusCode()).thenReturn(HttpStatusCode.valueOf(400));
        when(exception.getResponseBodyAs(ErrorResponse.class)).thenReturn(errorResponse);

        BaseApiClient baseApiClient = new BaseApiClient();
        AppResponse<Object> appResponse = baseApiClient.handleApiCall(() -> {
            throw exception;
        });
        assertThat(appResponse).isNotNull();
        assertThat(appResponse.getStatus().getCode()).isEqualTo("400");
        assertThat(appResponse.getStatus().getMessage()).isEqualTo(errorMsg);
    }

    @Test
    void testHandleApiCallWithRestClientExceptionWithNull() {
        String errorMsg = easyRandom.nextObject(String.class);

        RestClientResponseException exception = mock(RestClientResponseException.class);
        when(exception.getResponseBodyAs(ErrorResponse.class)).thenReturn(null);
        when(exception.getStatusCode()).thenReturn(HttpStatusCode.valueOf(500));
        when(exception.getLocalizedMessage()).thenReturn(errorMsg);

        BaseApiClient baseApiClient = new BaseApiClient();
        AppResponse<Object> appResponse = baseApiClient.handleApiCall(() -> {
            throw exception;
        });
        assertThat(appResponse).isNotNull();
        assertThat(appResponse.getStatus().getCode()).isEqualTo("500");
        assertThat(appResponse.getStatus().getMessage()).isEqualTo(errorMsg);
    }

    @Test
    void testHandleApiCallWithRestClientExceptionFailToParseResponseBody() {
        String errorMsg = easyRandom.nextObject(String.class);
        String parseErrorMsg = easyRandom.nextObject(String.class);

        RestClientResponseException exception = mock(RestClientResponseException.class);
        RuntimeException exp = mock(RuntimeException.class);
        when(exp.getLocalizedMessage()).thenReturn(parseErrorMsg);

        when(exception.getResponseBodyAs(ErrorResponse.class)).thenThrow(exp);
        when(exception.getStatusCode()).thenReturn(HttpStatusCode.valueOf(400));
        when(exception.getLocalizedMessage()).thenReturn(errorMsg);

        BaseApiClient baseApiClient = new BaseApiClient();
        AppResponse<Object> appResponse = baseApiClient.handleApiCall(() -> {
            throw exception;
        });
        assertThat(appResponse).isNotNull();
        assertThat(appResponse.getStatus().getCode()).isEqualTo("500");
        assertThat(appResponse.getStatus().getMessage()).isEqualTo("Invalid ErrorResponse");
    }
}