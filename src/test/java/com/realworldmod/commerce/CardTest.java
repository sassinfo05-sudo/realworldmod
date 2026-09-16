package com.realworldmod.commerce;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardTest {
    @Test
    void rankValueStartsAtTwo() {
        assertEquals(2, Card.Rank.TWO.value());
        assertEquals(14, Card.Rank.ACE.value());
        assertEquals(12, Card.Rank.QUEEN.value());
    }

    @Test
    void ordinalRoundTripsForEveryCard() {
        for (Card.Rank rank : Card.Rank.values()) {
            for (Card.Suit suit : Card.Suit.values()) {
                Card card = new Card(rank, suit);
                assertEquals(card, Card.fromOrdinal(card.toOrdinal()));
            }
        }
    }

    @Test
    void ordinalsAreUniquePerCard() {
        Set<Integer> ordinals = new HashSet<>();
        for (Card.Rank rank : Card.Rank.values()) {
            for (Card.Suit suit : Card.Suit.values()) {
                assertTrue(ordinals.add(new Card(rank, suit).toOrdinal()));
            }
        }
    }
}
