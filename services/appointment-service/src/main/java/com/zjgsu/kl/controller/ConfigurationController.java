package com.zjgsu.kl.controller;

import java.lang.management.ManagementFactory;
import java.util.Map;
import com.zjgsu.kl.config.BookingPolicy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ConfigurationController {
    private final BookingPolicy policy;
    ConfigurationController(BookingPolicy policy) { this.policy = policy; }
    @GetMapping("/api/appointments/config")
    Map<String, Object> configuration() {
        return Map.of("environment", policy.getEnvironment(), "revision", policy.getRevision(),
            "bookingEnabled", policy.isEnabled(), "startedAt", ManagementFactory.getRuntimeMXBean().getStartTime());
    }
}
