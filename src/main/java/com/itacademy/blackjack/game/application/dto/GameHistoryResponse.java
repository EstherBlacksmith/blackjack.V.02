package com.itacademy.blackjack.game.application.dto;

public record GameHistoryResponse(
        String gameId,
        String playedAt,
        String result,
        int playerScore,
        int dealerScore
) {}
