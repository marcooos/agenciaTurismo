package com.dev.turismo.agencia.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dev.turismo.agencia.model.Hospedagem;
import com.dev.turismo.agencia.model.Pacote;
import com.dev.turismo.agencia.model.Passagem;
import com.dev.turismo.agencia.repository.PacoteRepository;
import com.dev.turismo.agencia.service.FileStorageService;

@RestController
@RequestMapping("/api/pacotes")
@PreAuthorize("hasAnyRole('ADMIN','VENDEDOR')")
public class PacoteController {

    private final PacoteRepository repo;
    private final FileStorageService storage;

    public PacoteController(PacoteRepository repo, FileStorageService storage) {
        this.repo = repo;
        this.storage = storage;
    }

    @GetMapping
    public List<Pacote> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Pacote buscar(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();
    }

    @PostMapping
    public Pacote criar(@RequestBody Pacote p) {
        p.setId(null);
        return repo.save(p);
    }

    @PutMapping("/{id}")
    public Pacote atualizar(@PathVariable Long id, @RequestBody Pacote dto) {
        Pacote atual = repo.findById(id).orElseThrow();

        // Atualize apenas os campos editáveis no formulário
        atual.setTitulo(dto.getTitulo());
        atual.setDescricao(dto.getDescricao());
        atual.setPrecoBase(dto.getPrecoBase());

        if (dto.getPassagem() != null) {
            var p = atual.getPassagem();
            if (p == null) {
                p = new Passagem();
                atual.setPassagem(p);
            }
            p.setCompanhia(dto.getPassagem().getCompanhia());
            p.setOrigem(dto.getPassagem().getOrigem());
            p.setDestino(dto.getPassagem().getDestino());
            p.setDataIda(dto.getPassagem().getDataIda());
            p.setDataVolta(dto.getPassagem().getDataVolta());
        }

        if (dto.getHospedagem() != null) {
            var h = atual.getHospedagem();
            if (h == null) {
                h = new Hospedagem();
                atual.setHospedagem(h);
            }
            h.setHotel(dto.getHospedagem().getHotel());
            h.setCidade(dto.getHospedagem().getCidade());
            h.setNoites(dto.getHospedagem().getNoites());
        }
        return repo.save(atual);
    }

    @DeleteMapping("/{id}")
    public void apagar(@PathVariable Long id) {
        repo.deleteById(id);
    }

    @PostMapping("/{id}/imagem")
    public Pacote uploadImagem(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        var p = repo.findById(id).orElseThrow();
        String url = storage.savePacoteImage(file);
        p.setImagemUrl(url);
        return repo.save(p);
    }
}
