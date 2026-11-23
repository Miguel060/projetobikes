package com.bikerental.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bikerental.dto.LocacaoRequest;
import com.bikerental.dto.LocacaoResponse;
import com.bikerental.service.LocacaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/locacoes")
@RequiredArgsConstructor
@Tag(name = "Locações")
@SecurityRequirement(name = "bearerAuth")
public class LocacaoController {

    private final LocacaoService service;

    @GetMapping
    @Operation(summary = "Lista locações (ADMIN: todas; CLIENTE: as próprias)")
    public List<LocacaoResponse> listar(Authentication auth) {
        return service.listar(auth);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca locação por id")
    public LocacaoResponse buscar(@PathVariable Long id, Authentication auth) {
        return service.buscar(id, auth);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Aluga uma bicicleta disponível")
    public LocacaoResponse alugar(@Valid @RequestBody LocacaoRequest req, Authentication auth) {
        return service.alugar(req, auth);
    }

    @PatchMapping("/{id}/devolver")
    @Operation(summary = "Registra a devolução e calcula o valor (R$ 5,00 por hora iniciada)")
    public LocacaoResponse devolver(@PathVariable Long id, Authentication auth) {
        return service.devolver(id, auth);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove locação finalizada (ADMIN)")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
