package com.zjgsu.kl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
public class TokenPolicy {
    private final long expiresIn;
    public TokenPolicy(@Value("${hospital.auth.expires-in-seconds:1800}") long expiresIn) {
        if (expiresIn < 60 || expiresIn > 86400) {
            throw new IllegalArgumentException("JWT 有效期必须为 60 至 86400 秒");
        }
        this.expiresIn = expiresIn;
    }
    public long expiresIn() { return expiresIn; }
}
