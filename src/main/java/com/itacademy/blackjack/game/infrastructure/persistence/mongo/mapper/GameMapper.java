package com.itacademy.blackjack.game.infrastructure.persistence.mongo.mapper;

import com.itacademy.blackjack.game.domain.model.Game;
import com.itacademy.blackjack.game.infrastructure.persistence.mongo.document.GameDocument;
import org.springframework.stereotype.Component;


@Component
public interface GameMapper {
    GameDocument toDocument(Game game);
    Game toDomain(GameDocument document);
}
