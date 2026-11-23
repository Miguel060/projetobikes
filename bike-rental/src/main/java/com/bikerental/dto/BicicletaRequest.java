package com.bikerental.dto;

import com.bikerental.model.StatusBicicleta;

import jakarta.validation.constraints.NotBlank;

/** Status é opcional (padrão: DISPONIVEL). */
public record BicicletaRequest(@NotBlank String codigo, @NotBlank String modelo, StatusBicicleta status) {}
