package com.bikerental.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ClienteRequest(
        @NotBlank String nome,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "deve ter 11 dígitos, somente números") String cpf,
        String telefone,
        @NotBlank @Email String email,
        @NotNull Long usuarioId) {}
