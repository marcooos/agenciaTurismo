package com.dev.turismo.agencia.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.turismo.agencia.model.Adicional;
import com.dev.turismo.agencia.repository.AdicionalRepository;

@RestController
@RequestMapping("/api/adicionais")
public class AdicionalController {
    private final AdicionalRepository repo;

    public AdicionalController(AdicionalRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Adicional> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Adicional buscar(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();
    }

    @PostMapping
    public Adicional criar(@RequestBody Adicional a) {
        a.setId(null);
        return repo.save(a);
    }

    @PutMapping("/{id}")
    public Adicional atualizar(@PathVariable Long id, @RequestBody Adicional a) {
        a.setId(id);
        return repo.save(a);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void apagar(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
