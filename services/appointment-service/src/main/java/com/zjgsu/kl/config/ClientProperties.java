package com.zjgsu.kl.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "clients")
public record ClientProperties(String petBaseUrl, String doctorBaseUrl,
                               Duration connectTimeout, Duration readTimeout) {
    public ClientProperties {
        petBaseUrl = normalize(petBaseUrl);
        doctorBaseUrl = normalize(doctorBaseUrl);
        if (connectTimeout == null || connectTimeout.isZero() || connectTimeout.isNegative()
                || readTimeout == null || readTimeout.isZero() || readTimeout.isNegative()) {
            throw new IllegalArgumentException("HTTP超时必须为正数");
        }
    }

    private static String normalize(String value) {
        URI uri = URI.create(value);
        if (uri.getHost() == null || !("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))) {
            throw new IllegalArgumentException("服务地址必须为HTTP或HTTPS地址");
        }
        return value.replaceAll("/+$", "");
    }
}
