package com.itacademy.blackjack.deck.infrastructure;

import com.itacademy.blackjack.deck.model.Card;
import com.itacademy.blackjack.deck.model.CardRank;
import com.itacademy.blackjack.deck.model.Suit;
import com.itacademy.blackjack.game.domain.model.CardData;

public class CardMapper {

    /**
     * Convert CardData (persistence) to Card (domain)
     */
    public static Card toCard(CardData cardData) {
        if (cardData == null) {
            return null;
        }
        return new Card(
                CardRank.valueOf(cardData.rank()),
                Suit.valueOf(cardData.suit())
        );
    }

    /**
     * Convert Card (domain) to CardData (persistence)
     */
    public static CardData toCardData(Card card) {
        if (card == null) {
            return null;
        }
        return new CardData(
                card.getRank().name(),
                card.getSuit().name(),
                card.getNumericValue()
        );
    }
}
