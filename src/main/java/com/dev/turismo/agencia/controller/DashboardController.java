package com.dev.turismo.agencia.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.turismo.agencia.repository.ClienteRepository;
import com.dev.turismo.agencia.repository.PacoteRepository;
import com.dev.turismo.agencia.repository.VendaRepository;
import com.dev.turismo.agencia.repository.VendedorRepository;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("isAuthenticated()")
public class DashboardController {
    private final ClienteRepository clienteRepo;
    private final VendedorRepository vendedorRepo;
    private final PacoteRepository pacoteRepo;
    private final VendaRepository vendaRepo;

    public DashboardController(ClienteRepository clienteRepo, VendedorRepository vendedorRepo,
            PacoteRepository pacoteRepo, VendaRepository vendaRepo) {
        this.clienteRepo = clienteRepo;
        this.vendedorRepo = vendedorRepo;
        this.pacoteRepo = pacoteRepo;
        this.vendaRepo = vendaRepo;
    }

    @GetMapping("/resumo")
    public Map<String, Object> resumo() {
        return Map.of(
                "totalClientes", clienteRepo.count(),
                "totalVendedores", vendedorRepo.count(),
                "totalPacotes", pacoteRepo.count(),
                "totalVendas", vendaRepo.count(),
                "totalFaturamento", vendaRepo.somaFaturamento());
    }
}
