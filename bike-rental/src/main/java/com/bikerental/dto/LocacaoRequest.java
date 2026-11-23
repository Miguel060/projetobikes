package com.bikerental.dto;

import jakarta.validation.constraints.NotNull;

/** clienteId é obrigatório apenas para ADMIN; CLIENTE aluga sempre para si mesmo. */
public record LocacaoRequest(Long clienteId, @NotNull Long bicicletaId) {}
