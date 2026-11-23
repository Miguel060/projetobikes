package com.bikerental.dto;

import java.time.LocalDateTime;

import com.bikerental.model.Role;
import com.bikerental.model.Usuario;

public record UsuarioResponse(Long id, String username, Role role,
        LocalDateTime dataCriacao, String criadoPor,
        LocalDateTime dataModificacao, String modificadoPor) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getUsername(), u.getRole(),
                u.getDataCriacao(), u.getCriadoPor(), u.getDataModificacao(), u.getModificadoPor());
    }
}
