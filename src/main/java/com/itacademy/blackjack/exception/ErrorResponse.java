package com.itacademy.blackjack.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {
    // Getters
    private String message;
    private int status;

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }

}
