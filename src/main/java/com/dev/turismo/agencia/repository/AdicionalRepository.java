package com.dev.turismo.agencia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.turismo.agencia.model.Adicional;

@Repository
public interface AdicionalRepository extends JpaRepository<Adicional, Long> {
}
