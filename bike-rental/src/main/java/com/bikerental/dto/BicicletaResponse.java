package com.bikerental.dto;

import com.bikerental.model.Bicicleta;
import com.bikerental.model.StatusBicicleta;

public record BicicletaResponse(Long id, String codigo, String modelo, StatusBicicleta status) {

    public static BicicletaResponse from(Bicicleta b) {
        return new BicicletaResponse(b.getId(), b.getCodigo(), b.getModelo(), b.getStatus());
    }
}
