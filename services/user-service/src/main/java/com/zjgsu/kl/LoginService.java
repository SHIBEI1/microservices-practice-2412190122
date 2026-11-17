package com.zjgsu.kl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

@Service
class LoginService {
    private static final long EXPIRES_IN_SECONDS = 1800;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
    private final Map<String, Account> accounts;
    private final JwtEncoder encoder;

    LoginService(@Value("${hospital.auth.owner-password-hash}") String ownerHash,
                 @Value("${hospital.auth.admin-password-hash}") String adminHash,
                 @Value("${hospital.auth.jwt-secret}") String secret) {
        requireBcrypt(ownerHash);
        requireBcrypt(adminHash);
        accounts = Map.of("owner", new Account(ownerHash, "USER"), "admin", new Account(adminHash, "ADMIN"));
        byte[] key = Base64.getDecoder().decode(secret);
        if (key.length < 32) {
            throw new IllegalArgumentException("JWT 密钥解码后至少需要 32 字节");
        }
        encoder = NimbusJwtEncoder.withSecretKey(new SecretKeySpec(key, "HmacSHA256"))
            .algorithm(MacAlgorithm.HS256).build();
    }

    Optional<Token> authenticate(String username, String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return Optional.empty();
        }
        Account account = accounts.get(username);
        String hash = account == null ? accounts.get("owner").passwordHash() : account.passwordHash();
        if (!passwords.matches(password, hash) || account == null) {
            return Optional.empty();
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("pet-hospital-user-service").subject(username)
            .audience(List.of("pet-hospital-gateway")).issuedAt(now).expiresAt(now.plusSeconds(EXPIRES_IN_SECONDS))
            .claim("roles", List.of(account.role())).build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
        return Optional.of(new Token(token, "Bearer", EXPIRES_IN_SECONDS));
    }

    private static void requireBcrypt(String hash) {
        if (!hash.matches("\\$2[aby]\\$(1[2-6])\\$[./A-Za-z0-9]{53}")) {
            throw new IllegalArgumentException("测试账号需配置成本为 12 至 16 的 BCrypt 哈希");
        }
    }

    private record Account(String passwordHash, String role) {}

    record Token(String accessToken, String tokenType, long expiresIn) {}
}
