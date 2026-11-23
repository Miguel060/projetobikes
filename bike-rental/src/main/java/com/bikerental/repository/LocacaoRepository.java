package com.bikerental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bikerental.model.Locacao;
import com.bikerental.model.StatusLocacao;

public interface LocacaoRepository extends JpaRepository<Locacao, Long> {
    List<Locacao> findByClienteUsuarioUsername(String username);
    boolean existsByClienteIdAndStatus(Long clienteId, StatusLocacao status);
}
