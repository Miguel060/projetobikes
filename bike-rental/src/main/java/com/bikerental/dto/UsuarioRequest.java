package com.bikerental.dto;

import com.bikerental.model.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(max = 50) String username,
        @Size(min = 6, max = 100) String password,
        @NotNull Role role) {}
