package com.dev.turismo.agencia.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/cookie-demo")
public class CookieDemoController {

    @PostMapping("/set")
    public ResponseEntity<?> set(@RequestParam(defaultValue = "escuro") String tema,
            HttpServletResponse resp) {
        ResponseCookie cookie = ResponseCookie.from("preferenciaTema", tema)
                .httpOnly(false) // JS poderia ler (didático). Em produção prefira true quando possível.
                .secure(false) // true se estiver em HTTPS
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7)) // expira em 7 dias
                .build();
        resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(Map.of("ok", true, "tema", tema));
    }

    @GetMapping("/get")
    public Map<String, Object> get(@CookieValue(value = "preferenciaTema", required = false) String tema) {
        return Map.of("tema", tema == null ? "padrao" : tema);
    }

    @PostMapping("/del")
    public ResponseEntity<?> del(HttpServletResponse resp) {
        ResponseCookie cookie = ResponseCookie.from("preferenciaTema", "")
                .path("/")
                .maxAge(0) // apaga
                .build();
        resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }
}