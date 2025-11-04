package com.dev.turismo.agencia.controller;

import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.turismo.agencia.model.Venda;
import com.dev.turismo.agencia.repository.VendaRepository;
import com.dev.turismo.agencia.util.VendaMapper;


@RestController
@RequestMapping("/api/vendas")
public class VendaController {
    private final VendaRepository repo;
    private final VendaMapper mapper;

    public VendaController(VendaRepository repo, VendaMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @GetMapping
    public List<Venda> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Venda buscar(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();
    }

    @PostMapping
    public Venda criar(@RequestBody Venda v) {
        mapper.preencherEntidades(v);
        v.setId(null);
        return repo.save(v);
    }

    @PutMapping("/{id}")
    public Venda atualizar(@PathVariable Long id, @RequestBody Venda v) {
        var atual = repo.findById(id).orElseThrow();
        mapper.atualizarEntidades(v, atual);
        return repo.save(atual);
    }

    @DeleteMapping("/{id}")
    public void apagar(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
