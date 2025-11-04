package com.dev.turismo.agencia.util;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.dev.turismo.agencia.model.Adicional;
import com.dev.turismo.agencia.model.Venda;
import com.dev.turismo.agencia.repository.AdicionalRepository;
import com.dev.turismo.agencia.repository.ClienteRepository;
import com.dev.turismo.agencia.repository.PacoteRepository;
import com.dev.turismo.agencia.repository.VendedorRepository;

@Component
public class VendaMapper {
    private final ClienteRepository clienteRepo;
    private final VendedorRepository vendedorRepo;
    private final PacoteRepository pacoteRepo;
    private final AdicionalRepository adicionalRepo;

    public VendaMapper(ClienteRepository clienteRepo,
            VendedorRepository vendedorRepo,
            PacoteRepository pacoteRepo,
            AdicionalRepository adicionalRepo) {
        this.clienteRepo = clienteRepo;
        this.vendedorRepo = vendedorRepo;
        this.pacoteRepo = pacoteRepo;
        this.adicionalRepo = adicionalRepo;
    }

    public void preencherEntidades(Venda v) {
        v.setCliente(buscar(clienteRepo, v.getCliente()));
        v.setVendedor(buscar(vendedorRepo, v.getVendedor()));
        v.setPacote(buscar(pacoteRepo, v.getPacote()));
        v.setAdicionais(buscarAdicionais(v.getAdicionais()));
    }

    public void atualizarEntidades(Venda origem, Venda destino) {
        var cliente = buscar(clienteRepo, origem.getCliente());
        var vendedor = buscar(vendedorRepo, origem.getVendedor());
        var pacote = buscar(pacoteRepo, origem.getPacote());
        var adicionais = buscarAdicionais(origem.getAdicionais());

        if (cliente != null)
            destino.setCliente(cliente);
        if (vendedor != null)
            destino.setVendedor(vendedor);
        if (pacote != null)
            destino.setPacote(pacote);
        if (origem.getQuantidade() != null)
            destino.setQuantidade(origem.getQuantidade());
        if (adicionais != null)
            destino.setAdicionais(adicionais);
    }

    private <T> T buscar(JpaRepository<T, Long> repo, T entidade) {
        return Optional.ofNullable(entidade)
                .map(e -> repo.findById(getId(e)).orElseThrow())
                .orElse(null);
    }

    private List<Adicional> buscarAdicionais(List<Adicional> adicionais) {
        return Optional.ofNullable(adicionais)
                .map(list -> adicionalRepo.findAllById(list.stream()
                        .map(Adicional::getId)
                        .toList()))
                .orElse(null);
    }

    private Long getId(Object obj) {
        try {
            return (Long) obj.getClass().getMethod("getId").invoke(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException("Entidade sem método getId");
        }
    }
}
