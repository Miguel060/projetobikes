package com.bikerental.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bikerental.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByUsuarioUsername(String username);
    boolean existsByCpf(String cpf);
    boolean existsByUsuarioId(Long usuarioId);
}
