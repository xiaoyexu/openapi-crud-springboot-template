package com.xuxiaoye.api.services;

import com.xuxiaoye.api.adapter.api.server.dto.SearchStudentRequest;
import com.xuxiaoye.api.bean.Pagination;
import com.xuxiaoye.api.conf.ResourceConfig;
import com.xuxiaoye.api.services.interfaces.StudentService;
import com.xuxiaoye.api.utils.ContextUtils;
import com.xuxiaoye.api.utils.JwtUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;

@Log4j2
public class ScheduledTasks {

    @Value("${configs.appEnv}")
    String appEnv;

    private final ResourceConfig resourceConfig;

    private final StudentService studentService;

    public ScheduledTasks(
            ResourceConfig resourceConfig,
            StudentService studentService
    ) {
        this.resourceConfig = resourceConfig;
        this.studentService = studentService;
    }

    @Scheduled(cron = "${configs.scheduler.check}")
    public void autoCancelOvertimeOUnpaidRegistrations() {
        log.info("Check Payment Status Started");
        String scheduler = "scheduler";
        String newToken = JwtUtils.generateJWTToken(
                resourceConfig.getPrivateKey(),
                scheduler,
                Map.of(
                        "id", scheduler
                ),
                1800
        );

        ContextUtils.prepareRequestContext(appEnv, scheduler, newToken);
        this.studentService.searchStudent(new SearchStudentRequest(), Pagination.of(0, 10));
        log.info("Check Payment Status Finished");
    }

    @PostConstruct
    void postConstruct() {
        autoCancelOvertimeOUnpaidRegistrations();
    }
}
