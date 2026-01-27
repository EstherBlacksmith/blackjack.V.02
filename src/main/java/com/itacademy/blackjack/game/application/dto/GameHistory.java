package com.itacademy.blackjack.game.application.dto;

import com.itacademy.blackjack.game.domain.model.GameResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record GameHistory(
        UUID gameId,
        LocalDateTime playedAt,
        GameResult result,
        int playerScore,
        int dealerScore
) {}