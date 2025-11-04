package com.dev.turismo.agencia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dev.turismo.agencia.model.Venda;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {
    @Query("select coalesce(sum(v.valorTotal),0) from Venda v")
    java.math.BigDecimal somaFaturamento();
}

