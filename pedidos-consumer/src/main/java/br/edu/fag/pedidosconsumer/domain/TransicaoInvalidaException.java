package br.edu.fag.pedidosconsumer.domain;

public class TransicaoInvalidaException extends RuntimeException {

    public TransicaoInvalidaException(String message) {
        super(message);
    }
}
