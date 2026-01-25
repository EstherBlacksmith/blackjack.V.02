package com.itacademy.blackjack.game.application.dto;


import com.itacademy.blackjack.game.domain.model.PlayerStatus;

import java.util.List;
import java.util.UUID;

public record PlayerResponse(
        UUID id,
        String name,
        List<CardResponse> hand,
        int score,
        PlayerStatus status
) {
}