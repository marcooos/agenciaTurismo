package com.dev.turismo.agencia.controller;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.dev.turismo.agencia.dto.LoginDTO;
import com.dev.turismo.agencia.repository.VendedorRepository;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManager authManager;
    private final VendedorRepository vendedorRepo;

    public AuthController(AuthenticationManager am,
            VendedorRepository vr) {
        this.authManager = am;
        this.vendedorRepo = vr;        
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha()));
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context); // com requireExplicitSave(false) já persiste na sessão

            var v = vendedorRepo.findByEmail(dto.getEmail()).orElseThrow();
            return ResponseEntity.ok(Map.of(
                    "id", v.getId(), "nome", v.getNome(), "email", v.getEmail(), "role", v.getRole()));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Credenciais inválidas"));
        }
    }

    @GetMapping("/auth/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var v = vendedorRepo.findByEmail(auth.getName()).orElse(null);
        if (v == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(Map.of(
                "id", v.getId(), "nome", v.getNome(), "email", v.getEmail(), "role", v.getRole()));
    }
}