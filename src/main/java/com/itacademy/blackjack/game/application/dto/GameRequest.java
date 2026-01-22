package com.itacademy.blackjack.game.application.dto;

import jakarta.validation.Valid;

import java.util.UUID;

public record GameRequest(@Valid UUID playerId) {

}
