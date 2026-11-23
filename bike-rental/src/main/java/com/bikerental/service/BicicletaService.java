package com.bikerental.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bikerental.dto.BicicletaRequest;
import com.bikerental.dto.BicicletaResponse;
import com.bikerental.exception.ApiException;
import com.bikerental.model.Bicicleta;
import com.bikerental.model.StatusBicicleta;
import com.bikerental.repository.BicicletaRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BicicletaService {

    private final BicicletaRepository repository;

    @Transactional(readOnly = true)
    public List<BicicletaResponse> listar() {
        return repository.findAll().stream().map(BicicletaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BicicletaResponse buscar(Long id) {
        return BicicletaResponse.from(obter(id));
    }

    public BicicletaResponse criar(BicicletaRequest req) {
        if (repository.existsByCodigo(req.codigo())) {
            throw ApiException.conflito("Código já cadastrado");
        }
        if (req.status() == StatusBicicleta.ALUGADA) {
            throw ApiException.regra("O status ALUGADA é definido apenas ao criar uma locação");
        }
        Bicicleta b = new Bicicleta();
        b.setCodigo(req.codigo());
        b.setModelo(req.modelo());
        if (req.status() != null) {
            b.setStatus(req.status());
        }
        return BicicletaResponse.from(repository.save(b));
    }

    public BicicletaResponse atualizar(Long id, BicicletaRequest req) {
        Bicicleta b = obter(id);
        if (!b.getCodigo().equals(req.codigo()) && repository.existsByCodigo(req.codigo())) {
            throw ApiException.conflito("Código já cadastrado");
        }
        if (req.status() != null && req.status() != b.getStatus()) {
            if (b.getStatus() == StatusBicicleta.ALUGADA) {
                throw ApiException.regra("Bicicleta alugada: o status muda apenas com a devolução");
            }
            if (req.status() == StatusBicicleta.ALUGADA) {
                throw ApiException.regra("O status ALUGADA é definido apenas ao criar uma locação");
            }
            b.setStatus(req.status());
        }
        b.setCodigo(req.codigo());
        b.setModelo(req.modelo());
        return BicicletaResponse.from(repository.save(b));
    }

    public void deletar(Long id) {
        Bicicleta b = obter(id);
        if (b.getStatus() == StatusBicicleta.ALUGADA) {
            throw ApiException.regra("Não é possível excluir uma bicicleta alugada");
        }
        repository.delete(b);
        repository.flush(); // falha com 409 se houver locações
    }

    private Bicicleta obter(Long id) {
        return repository.findById(id).orElseThrow(() -> ApiException.naoEncontrado("Bicicleta não encontrada"));
    }
}
