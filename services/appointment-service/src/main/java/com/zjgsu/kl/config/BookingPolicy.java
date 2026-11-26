package com.zjgsu.kl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class BookingPolicy {
    @Value("${hospital.booking.enabled:true}") private boolean enabled;
    @Value("${hospital.config.environment:unit-test}") private String environment;
    @Value("${hospital.config.revision:local}") private String revision;
    public boolean isEnabled() { return enabled; }
    public String getEnvironment() { return environment; }
    public String getRevision() { return revision; }
}
