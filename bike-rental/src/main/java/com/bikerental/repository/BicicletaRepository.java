package com.bikerental.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bikerental.model.Bicicleta;

public interface BicicletaRepository extends JpaRepository<Bicicleta, Long> {
    boolean existsByCodigo(String codigo);
}
