package com.itacademy.blackjack.game.dto;

import com.itacademy.blackjack.game.model.GameResult;
import com.itacademy.blackjack.game.model.GameStatus;

import java.util.List;
import java.util.UUID;

public record GameResponse(
        UUID id,
        GameStatus status,
        GameResult result,
        PlayerResponse player,
        List<CardResponse> crupierHand,
        int crupierScore
) {}