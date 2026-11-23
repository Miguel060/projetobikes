package com.bikerental.dto;

import java.time.LocalDateTime;

import com.bikerental.model.Cliente;

public record ClienteResponse(Long id, String nome, String cpf, String telefone, String email, Long usuarioId,
        LocalDateTime dataCriacao, String criadoPor,
        LocalDateTime dataModificacao, String modificadoPor) {

    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNome(), c.getCpf(), c.getTelefone(), c.getEmail(),
                c.getUsuario().getId(),
                c.getDataCriacao(), c.getCriadoPor(), c.getDataModificacao(), c.getModificadoPor());
    }
}
