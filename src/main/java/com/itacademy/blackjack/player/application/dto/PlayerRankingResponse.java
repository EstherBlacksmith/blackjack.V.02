package com.itacademy.blackjack.player.application.dto;

public record PlayerRankingResponse(
        int rank,
        String playerId,
        String playerName,
        int wins,
        int losses,
        int pushes
) {}
