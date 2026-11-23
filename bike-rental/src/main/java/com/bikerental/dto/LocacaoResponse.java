package com.bikerental.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bikerental.model.Locacao;
import com.bikerental.model.StatusLocacao;

public record LocacaoResponse(Long id, Long clienteId, String clienteNome, Long bicicletaId, String bicicletaCodigo,
        LocalDateTime dataRetirada, LocalDateTime dataDevolucao, BigDecimal valor, StatusLocacao status) {

    public static LocacaoResponse from(Locacao l) {
        return new LocacaoResponse(l.getId(),
                l.getCliente().getId(), l.getCliente().getNome(),
                l.getBicicleta().getId(), l.getBicicleta().getCodigo(),
                l.getDataRetirada(), l.getDataDevolucao(), l.getValor(), l.getStatus());
    }
}
