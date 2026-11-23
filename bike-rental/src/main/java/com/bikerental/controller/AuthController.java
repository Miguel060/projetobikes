package com.bikerental.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.bikerental.dto.LoginRequest;
import com.bikerental.dto.LoginResponse;
import com.bikerental.exception.ApiException;
import com.bikerental.model.Usuario;
import com.bikerental.repository.UsuarioRepository;
import com.bikerental.security.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Autentica e devolve o token JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        Usuario u = usuarioRepository.findByUsername(req.username())
                .filter(user -> encoder.matches(req.password(), user.getPassword()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos"));
        return new LoginResponse(jwtService.gerarToken(u.getUsername(), u.getRole()), u.getUsername(), u.getRole());
    }
}
