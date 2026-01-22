package com.itacademy.blackjack.game.dto;

import jakarta.validation.Valid;

import java.util.UUID;

public record GameRequest(@Valid UUID playerId) {

}
