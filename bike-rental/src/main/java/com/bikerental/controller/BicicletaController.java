package com.bikerental.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.bikerental.dto.BicicletaRequest;
import com.bikerental.dto.BicicletaResponse;
import com.bikerental.service.BicicletaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bicicletas")
@RequiredArgsConstructor
@Tag(name = "Bicicletas")
@SecurityRequirement(name = "bearerAuth")
public class BicicletaController {

    private final BicicletaService service;

    @GetMapping
    @Operation(summary = "Lista bicicletas e seus status")
    public List<BicicletaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca bicicleta por id")
    public BicicletaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra bicicleta (ADMIN)")
    public BicicletaResponse criar(@Valid @RequestBody BicicletaRequest req) {
        return service.criar(req);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza bicicleta (ADMIN)")
    public BicicletaResponse atualizar(@PathVariable Long id, @Valid @RequestBody BicicletaRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove bicicleta (ADMIN)")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }
}
