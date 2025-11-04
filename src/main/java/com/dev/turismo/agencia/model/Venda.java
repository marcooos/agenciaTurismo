package com.dev.turismo.agencia.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Cliente cliente;
    @ManyToOne(optional = false)
    private Vendedor vendedor;
    @ManyToOne(optional = false)
    private Pacote pacote;


    private Integer quantidade=1;

    @ManyToMany
    @JoinTable(name = "venda_adicionais",joinColumns = @JoinColumn(name = "venda_id"),inverseJoinColumns = @JoinColumn(name = "adicional_id"))
    private List<Adicional> adicionais;
    private BigDecimal valorTotal;
    private LocalDateTime dataVenda = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    public void calcularTotal() {
        BigDecimal base = (pacote != null && pacote.getPrecoBase() != null) ? pacote.getPrecoBase() : BigDecimal.ZERO;
        BigDecimal somaAdicionais = BigDecimal.ZERO;
        if (adicionais != null) {
            for (Adicional a : adicionais) {
                if (a.getPreco() != null)
                    somaAdicionais = somaAdicionais.add(a.getPreco());
            }
        }
        this.valorTotal = base.multiply(BigDecimal.valueOf(quantidade == null ? 1 : quantidade))
                .add(somaAdicionais);
    }
}
