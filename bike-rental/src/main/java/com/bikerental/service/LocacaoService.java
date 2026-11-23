package com.bikerental.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bikerental.dto.LocacaoRequest;
import com.bikerental.dto.LocacaoResponse;
import com.bikerental.exception.ApiException;
import com.bikerental.model.*;
import com.bikerental.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class LocacaoService {

    /** Regra simples de preço: R$ 5,00 por hora iniciada (mínimo de 1 hora). */
    private static final BigDecimal VALOR_HORA = new BigDecimal("5.00");

    private final LocacaoRepository repository;
    private final ClienteRepository clienteRepository;
    private final BicicletaRepository bicicletaRepository;

    @Transactional(readOnly = true)
    public List<LocacaoResponse> listar(Authentication auth) {
        List<Locacao> lista = isAdmin(auth)
                ? repository.findAll()
                : repository.findByClienteUsuarioUsername(auth.getName());
        return lista.stream().map(LocacaoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public LocacaoResponse buscar(Long id, Authentication auth) {
        return LocacaoResponse.from(obterComAcesso(id, auth));
    }

    public LocacaoResponse alugar(LocacaoRequest req, Authentication auth) {
        Cliente cliente;
        if (isAdmin(auth)) {
            if (req.clienteId() == null) {
                throw ApiException.regra("clienteId é obrigatório para ADMIN");
            }
            cliente = clienteRepository.findById(req.clienteId())
                    .orElseThrow(() -> ApiException.naoEncontrado("Cliente não encontrado"));
        } else {
            cliente = clienteRepository.findByUsuarioUsername(auth.getName())
                    .orElseThrow(() -> ApiException.regra("Usuário não possui cadastro de cliente"));
        }

        Bicicleta bicicleta = bicicletaRepository.findById(req.bicicletaId())
                .orElseThrow(() -> ApiException.naoEncontrado("Bicicleta não encontrada"));
        if (bicicleta.getStatus() != StatusBicicleta.DISPONIVEL) {
            throw ApiException.conflito("Bicicleta não está disponível");
        }
        if (repository.existsByClienteIdAndStatus(cliente.getId(), StatusLocacao.ATIVA)) {
            throw ApiException.conflito("Cliente já possui uma locação ativa");
        }

        bicicleta.setStatus(StatusBicicleta.ALUGADA);
        Locacao locacao = new Locacao();
        locacao.setCliente(cliente);
        locacao.setBicicleta(bicicleta);
        locacao.setDataRetirada(LocalDateTime.now());
        locacao.setStatus(StatusLocacao.ATIVA);
        return LocacaoResponse.from(repository.save(locacao));
    }

    public LocacaoResponse devolver(Long id, Authentication auth) {
        Locacao locacao = obterComAcesso(id, auth);
        if (locacao.getStatus() != StatusLocacao.ATIVA) {
            throw ApiException.conflito("Locação já finalizada");
        }
        LocalDateTime agora = LocalDateTime.now();
        locacao.setDataDevolucao(agora);
        locacao.setValor(calcularValor(locacao.getDataRetirada(), agora));
        locacao.setStatus(StatusLocacao.FINALIZADA);
        locacao.getBicicleta().setStatus(StatusBicicleta.DISPONIVEL);
        return LocacaoResponse.from(locacao);
    }

    public void deletar(Long id) {
        Locacao locacao = repository.findById(id)
                .orElseThrow(() -> ApiException.naoEncontrado("Locação não encontrada"));
        if (locacao.getStatus() == StatusLocacao.ATIVA) {
            throw ApiException.regra("Devolva a bicicleta antes de excluir a locação");
        }
        repository.delete(locacao);
    }

    private BigDecimal calcularValor(LocalDateTime retirada, LocalDateTime devolucao) {
        long minutos = Duration.between(retirada, devolucao).toMinutes();
        long horas = Math.max(1, (minutos + 59) / 60);
        return VALOR_HORA.multiply(BigDecimal.valueOf(horas));
    }

    private Locacao obterComAcesso(Long id, Authentication auth) {
        Locacao locacao = repository.findById(id)
                .orElseThrow(() -> ApiException.naoEncontrado("Locação não encontrada"));
        boolean dono = locacao.getCliente().getUsuario().getUsername().equals(auth.getName());
        if (!isAdmin(auth) && !dono) {
            throw ApiException.proibido("Você não tem acesso a esta locação");
        }
        return locacao;
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
