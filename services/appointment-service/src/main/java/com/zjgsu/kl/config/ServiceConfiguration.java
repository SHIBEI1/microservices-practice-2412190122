package com.zjgsu.kl.config;

import feign.Request;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ClientProperties.class)
public class ServiceConfiguration {
    @Bean
    public Request.Options feignOptions(ClientProperties properties) {
        return new Request.Options(properties.connectTimeout(), properties.readTimeout(), false);
    }

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Shanghai"));
    }
}
