package com.itacademy.blackjack.player.application.dto;

import com.itacademy.blackjack.game.domain.model.PlayerStatus;

import java.util.UUID;

public record PlayerProfileResponse(
        UUID id,
        String name,
        PlayerStatus status,
        int wins,
        int losses,
        int pushes
) {}
