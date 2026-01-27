package com.itacademy.blackjack.game.domain.model.exception;


public class NotPlayerTurnException extends RuntimeException {
    public NotPlayerTurnException(String message) {
        super(message);
    }
}
