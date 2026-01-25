package com.itacademy.blackjack.player.application.dto;
import jakarta.validation.constraints.NotBlank;

public record CreatePlayerRequest(@NotBlank String name) {}
