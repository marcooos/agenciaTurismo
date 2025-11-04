package com.dev.turismo.agencia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dev.turismo.agencia.model.Vendedor;

@Repository
public interface VendedorRepository extends JpaRepository<Vendedor, Long> {
    Optional<Vendedor> findByEmailAndSenha(String email, String senha);

    boolean existsByEmail(String email);

    @Query("select coalesce(sum(v.valorTotal), 0) from Venda v")
    java.math.BigDecimal somaFaturamento();

    Optional<Vendedor> findByEmail(String email);
}
