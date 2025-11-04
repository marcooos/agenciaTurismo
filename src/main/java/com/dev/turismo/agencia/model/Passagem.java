package com.dev.turismo.agencia.model;

import java.time.LocalDate;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Passagem {
    private String companhia;
    private String origem;
    private String destino;
    private LocalDate dataIda;
    private LocalDate dataVolta;
}
