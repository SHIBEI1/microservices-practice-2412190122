package com.zjgsu.kl;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
class LoginController {
    private final LoginService login;

    LoginController(LoginService login) {
        this.login = login;
    }

    @PostMapping("/api/auth/login")
    ResponseEntity<LoginService.Token> authenticate(@Valid @RequestBody Credentials credentials) {
        return login.authenticate(credentials.username(), credentials.password())
            .map(token -> ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(token))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户名或密码错误"));
    }

    record Credentials(@NotBlank @Size(max = 40) String username,
                       @NotBlank @Size(max = 72) String password) {}
}
