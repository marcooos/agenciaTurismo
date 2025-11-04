package com.dev.turismo.agencia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.turismo.agencia.model.Pacote;

@Repository
public interface PacoteRepository extends JpaRepository<Pacote, Long> {
}