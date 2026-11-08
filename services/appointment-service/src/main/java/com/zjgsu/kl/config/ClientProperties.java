package com.zjgsu.kl.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clients")
public record ClientProperties(Duration connectTimeout, Duration readTimeout) {
    public ClientProperties {
        if (connectTimeout == null || connectTimeout.isZero() || connectTimeout.isNegative()
                || readTimeout == null || readTimeout.isZero() || readTimeout.isNegative()) {
            throw new IllegalArgumentException("HTTP超时必须为正数");
        }
    }
}
