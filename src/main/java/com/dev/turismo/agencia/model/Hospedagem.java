package com.dev.turismo.agencia.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hospedagem {
    private String hotel;
    private String cidade;
    private Integer noites;
}
