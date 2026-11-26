package com.zjgsu.kl;

import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
class GatewaySecurity {
    @Bean
    ReactiveJwtDecoder jwtDecoder(@Value("${hospital.auth.jwt-secret}") String secret) {
        byte[] key = Base64.getDecoder().decode(secret);
        if (key.length < 32) {
            throw new IllegalArgumentException("JWT 密钥解码后至少需要 32 字节");
        }
        var decoder = NimbusReactiveJwtDecoder.withSecretKey(new SecretKeySpec(key, "HmacSHA256"))
            .macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer("pet-hospital-user-service"),
            new JwtAudienceValidator("pet-hospital-gateway")));
        return decoder;
    }

    @Bean
    SecurityWebFilterChain security(ServerHttpSecurity http) {
        var roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");
        var authentication = new ReactiveJwtAuthenticationConverter();
        authentication.setJwtGrantedAuthoritiesConverter(new ReactiveJwtGrantedAuthoritiesConverterAdapter(roles));

        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((exchange, ignored) -> error(exchange, HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler((exchange, ignored) -> error(exchange, HttpStatus.FORBIDDEN)))
            .authorizeExchange(rules -> rules
                .pathMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/appointments/config").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/api/pets", "/api/pets/*", "/api/doctors", "/api/doctors/*",
                    "/api/appointments", "/api/appointments/*").hasAnyRole("USER", "ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/pets", "/api/appointments", "/api/appointments/*/cancel")
                    .hasAnyRole("USER", "ADMIN")
                .pathMatchers(HttpMethod.POST, "/api/doctors", "/api/appointments/*/confirm",
                    "/api/appointments/*/complete").hasRole("ADMIN")
                .anyExchange().denyAll())
            .oauth2ResourceServer(resource -> resource
                .authenticationEntryPoint((exchange, ignored) -> error(exchange, HttpStatus.UNAUTHORIZED))
                .jwt(jwt -> jwt.jwtAuthenticationConverter(authentication)))
            .build();
    }

    private Mono<Void> error(ServerWebExchange exchange, HttpStatus status) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        if (status == HttpStatus.UNAUTHORIZED) {
            response.getHeaders().set("WWW-Authenticate", "Bearer");
        }
        String detail = status == HttpStatus.UNAUTHORIZED ? "需要有效的访问令牌" : "当前角色无权访问";
        byte[] json = ("{\"status\":" + status.value() + ",\"detail\":\"" + detail + "\"}")
            .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(json)));
    }
}
