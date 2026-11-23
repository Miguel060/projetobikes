package com.bikerental.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bikerental.dto.UsuarioRequest;
import com.bikerental.dto.UsuarioResponse;
import com.bikerental.exception.ApiException;
import com.bikerental.model.Usuario;
import com.bikerental.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(UsuarioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        return UsuarioResponse.from(obter(id));
    }

    public UsuarioResponse criar(UsuarioRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw ApiException.regra("A senha é obrigatória");
        }
        if (repository.existsByUsername(req.username())) {
            throw ApiException.conflito("Username já cadastrado");
        }
        Usuario u = new Usuario();
        u.setUsername(req.username());
        u.setPassword(encoder.encode(req.password()));
        u.setRole(req.role());
        return UsuarioResponse.from(repository.saveAndFlush(u));
    }

    public UsuarioResponse atualizar(Long id, UsuarioRequest req) {
        Usuario u = obter(id);
        if (!u.getUsername().equals(req.username()) && repository.existsByUsername(req.username())) {
            throw ApiException.conflito("Username já cadastrado");
        }
        u.setUsername(req.username());
        u.setRole(req.role());
        if (req.password() != null && !req.password().isBlank()) {
            u.setPassword(encoder.encode(req.password()));
        }
        return UsuarioResponse.from(repository.saveAndFlush(u));
    }

    public void deletar(Long id) {
        repository.delete(obter(id));
        repository.flush(); // falha com 409 se houver cliente vinculado
    }

    private Usuario obter(Long id) {
        return repository.findById(id).orElseThrow(() -> ApiException.naoEncontrado("Usuário não encontrado"));
    }
}
