package com.itacademy.blackjack.game.domain.model.exception;

public class MissingIdentifierException extends RuntimeException {
    public MissingIdentifierException(String message) {
        super(message);
    }
}
