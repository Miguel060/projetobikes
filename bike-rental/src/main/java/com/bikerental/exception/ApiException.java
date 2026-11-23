package com.bikerental.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/** Erro de negócio/acesso com o status HTTP correspondente. */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public static ApiException naoEncontrado(String mensagem) {
        return new ApiException(HttpStatus.NOT_FOUND, mensagem);
    }

    public static ApiException regra(String mensagem) {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }

    public static ApiException conflito(String mensagem) {
        return new ApiException(HttpStatus.CONFLICT, mensagem);
    }

    public static ApiException proibido(String mensagem) {
        return new ApiException(HttpStatus.FORBIDDEN, mensagem);
    }
}
