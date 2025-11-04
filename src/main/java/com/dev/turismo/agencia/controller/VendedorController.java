package com.dev.turismo.agencia.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.turismo.agencia.model.Role;
import com.dev.turismo.agencia.model.Vendedor;
import com.dev.turismo.agencia.repository.VendedorRepository;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/vendedores")
@PreAuthorize("hasRole('ADMIN')")
public class VendedorController {

    private final VendedorRepository repo;

    public VendedorController(VendedorRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Vendedor> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Vendedor buscar(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();
    }

    @PostMapping
    public Vendedor criar(@RequestBody Vendedor v) {
        // perfil
        if (v.getRole() == null)
            v.setRole(Role.VENDEDOR);

        // senha: se não vier com prefixo {…}, prefixa {noop} (didático)
        v.setSenha(normalizeSenha(v.getSenha()));

        v.setId(null);
        return repo.save(v);
    }

    @PutMapping("/{id}")
    public Vendedor atualizar(@PathVariable Long id, @RequestBody Vendedor dto) {
        Vendedor atual = repo.findById(id).orElseThrow();

        if (dto.getNome() != null)
            atual.setNome(dto.getNome());
        if (dto.getEmail() != null)
            atual.setEmail(dto.getEmail());
        if (dto.getRole() != null)
            atual.setRole(dto.getRole());

        // Se senha veio informada, atualiza; se veio vazia/null, mantém a atual
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            atual.setSenha(normalizeSenha(dto.getSenha()));
        }

        return repo.save(atual);
    }

    @DeleteMapping("/{id}")
    public void apagar(@PathVariable Long id) {
        repo.deleteById(id);
    }

    // ===== helpers =====
    private String normalizeSenha(String raw) {
        if (raw == null || raw.isBlank())
            return raw;
        // se já vier {bcrypt}... ou {noop}..., mantém
        if (raw.startsWith("{"))
            return raw;
        // didático: salvar como texto puro com prefixo {noop}
        return "{noop}" + raw;

        /*
         * // ALTERNATIVA RECOMENDADA: usar BCrypt (precisa de PasswordEncoder bean)
         * // return passwordEncoder.encode(raw);
         */
    }
}
