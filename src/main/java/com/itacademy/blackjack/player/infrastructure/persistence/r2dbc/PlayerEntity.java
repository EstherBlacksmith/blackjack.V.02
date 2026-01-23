package com.itacademy.blackjack.player.infrastructure.persistence.r2dbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("players")
public record PlayerEntity(
        @Id String id,
        String name,
        int wins,
        int losses,
        int pushes
) {}