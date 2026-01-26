package com.itacademy.blackjack.player.application.dto;

import com.itacademy.blackjack.game.application.dto.GameHistoryResponse;

import java.util.List;

public record PlayerStatsResponse(
        int totalGames,
        int wins,
        int losses,
        int pushes,
        double winRate,
        int currentStreak,
        List<GameHistoryResponse> recentGames
) {}