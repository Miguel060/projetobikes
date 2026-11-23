package com.bikerental.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bikerental.dto.ClienteRequest;
import com.bikerental.dto.ClienteResponse;
import com.bikerental.service.ClienteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteService service;

    @GetMapping("/me")
    @Operation(summary = "Dados do cliente logado")
    public ClienteResponse me(Authentication auth) {
        return service.buscarPorUsername(auth.getName());
    }

    @GetMapping
    @Operation(summary = "Lista clientes (ADMIN)")
    public List<ClienteResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca cliente por id (ADMIN)")
    public ClienteResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria cliente vinculado a um usuário CLIENTE (ADMIN)")
    public ClienteResponse criar(@Valid @RequestBody ClienteRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza cliente (ADMIN)")
    public ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove cliente (ADMIN)")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
