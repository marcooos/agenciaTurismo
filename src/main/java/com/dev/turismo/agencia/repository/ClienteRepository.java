package com.dev.turismo.agencia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.turismo.agencia.model.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}