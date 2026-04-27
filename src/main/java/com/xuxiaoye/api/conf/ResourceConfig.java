package com.xuxiaoye.api.conf;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

public class ResourceConfig {

    @Value("${servicePublicKeyPath}")
    private String servicePublicKeyPath;

    @Value("${servicePrivateKeyPath}")
    private String servicePrivateKeyPath;

    @PostConstruct
    void postConstruct() throws Exception {
        this.publicKey = com.xuxiaoye.api.utils.FileUtils.readFileToBytes(servicePublicKeyPath);
        this.privateKey = com.xuxiaoye.api.utils.FileUtils.readFileToBytes(servicePrivateKeyPath);
    }

    @Getter
    private byte[] publicKey;

    @Getter
    private byte[] privateKey;
}
