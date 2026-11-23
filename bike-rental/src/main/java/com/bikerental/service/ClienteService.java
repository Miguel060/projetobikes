package com.bikerental.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bikerental.dto.ClienteRequest;
import com.bikerental.dto.ClienteResponse;
import com.bikerental.exception.ApiException;
import com.bikerental.model.Cliente;
import com.bikerental.model.Role;
import com.bikerental.model.Usuario;
import com.bikerental.repository.ClienteRepository;
import com.bikerental.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository repository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return repository.findAll().stream().map(ClienteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id) {
        return ClienteResponse.from(obter(id));
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorUsername(String username) {
        return repository.findByUsuarioUsername(username)
                .map(ClienteResponse::from)
                .orElseThrow(() -> ApiException.naoEncontrado("Usuário não possui cadastro de cliente"));
    }

    public ClienteResponse criar(ClienteRequest req) {
        if (repository.existsByCpf(req.cpf())) {
            throw ApiException.conflito("CPF já cadastrado");
        }
        Cliente c = new Cliente();
        preencher(c, req, obterUsuarioLivre(req.usuarioId()));
        return ClienteResponse.from(repository.saveAndFlush(c));
    }

    public ClienteResponse atualizar(Long id, ClienteRequest req) {
        Cliente c = obter(id);
        if (!c.getCpf().equals(req.cpf()) && repository.existsByCpf(req.cpf())) {
            throw ApiException.conflito("CPF já cadastrado");
        }
        Usuario usuario = c.getUsuario();
        if (!usuario.getId().equals(req.usuarioId())) {
            usuario = obterUsuarioLivre(req.usuarioId());
        }
        preencher(c, req, usuario);
        return ClienteResponse.from(repository.saveAndFlush(c));
    }

    public void deletar(Long id) {
        repository.delete(obter(id));
        repository.flush(); // falha com 409 se houver locações
    }

    private void preencher(Cliente c, ClienteRequest req, Usuario usuario) {
        c.setNome(req.nome());
        c.setCpf(req.cpf());
        c.setTelefone(req.telefone());
        c.setEmail(req.email());
        c.setUsuario(usuario);
    }

    private Usuario obterUsuarioLivre(Long usuarioId) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> ApiException.naoEncontrado("Usuário não encontrado"));
        if (u.getRole() != Role.CLIENTE) {
            throw ApiException.regra("O usuário vinculado deve ter o perfil CLIENTE");
        }
        if (repository.existsByUsuarioId(usuarioId)) {
            throw ApiException.conflito("Usuário já está vinculado a outro cliente");
        }
        return u;
    }

    private Cliente obter(Long id) {
        return repository.findById(id).orElseThrow(() -> ApiException.naoEncontrado("Cliente não encontrado"));
    }
}
