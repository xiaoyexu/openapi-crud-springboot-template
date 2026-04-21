package com.xuxiaoye.api.client;

import java.util.function.Function;
import java.util.function.Supplier;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.client.RestClientResponseException;

import com.xuxiaoye.api.resp.AppResponse;
import com.xuxiaoye.api.resp.AppStatus;
import com.xuxiaoye.api.resp.ErrorResponse;
import com.xuxiaoye.api.resp.ResponseStatus;

@Log4j2
public class BaseApiClient {
    protected <T> AppResponse<T> handleApiCall(Supplier<AppResponse<T>> logic, Function<RestClientResponseException, AppResponse<T>> handler) {
        try {
            return logic.get();
        } catch (RestClientResponseException ex) {
            log.error("Call api error: {}", ex.getLocalizedMessage());
            return handler.apply(ex);
        } catch (RuntimeException ex) {
            log.error("Call api runtime error: {}", ex.getLocalizedMessage());
            AppStatus appStatus = AppStatus.builder().code("500").message(ex.getLocalizedMessage()).build();
            return AppResponse.failWithStatus(appStatus);
        }
    }

    protected <T> AppResponse<T> handleApiCall(Supplier<AppResponse<T>> logic) {
        try {
            return logic.get();
        } catch (RestClientResponseException ex) {
            log.error("Call api error: {}", ex.getLocalizedMessage());
            int statusCode = ex.getStatusCode().value();
            AppStatus appStatus = AppStatus.builder().code(String.valueOf(statusCode)).message(ex.getLocalizedMessage()).build();
            try {
                ErrorResponse errorResponse = ex.getResponseBodyAs(ErrorResponse.class);
                if (errorResponse != null) {
                    ResponseStatus responseStatus = errorResponse.getStatus();
                    appStatus = AppStatus.builder().code(responseStatus.getCode()).message(responseStatus.getMessage()).build();
                }
            } catch (Exception e) {
                log.error("Fail to parse to ErrorResponse, error: {}", e.getLocalizedMessage());
                return AppResponse.failWithStatus(AppStatus.builder().code("500").message("Invalid ErrorResponse").build());
            }
            return AppResponse.failWithStatus(appStatus);
        } catch (RuntimeException e) {
            AppStatus appStatus = AppStatus.builder().code("500").message(e.getLocalizedMessage()).build();
            return AppResponse.failWithStatus(appStatus);
        }
    }
}
