package com.itacademy.blackjack.game.application.dto;

import com.itacademy.blackjack.game.domain.model.GameResult;
import com.itacademy.blackjack.game.domain.model.GameStatus;

import java.util.List;
import java.util.UUID;

public record GameResponse(
        UUID id,
        GameStatus status,
        GameResult result,
        PlayerResponse player,
        List<CardResponse> crupierHand,
        int crupierScore
) {
    }
