package com.realworldmod.commerce;

/**
 * A standard playing card, used by {@link ThreeCardPokerGame} — unlike
 * {@code BlackjackGame.Rank}, poker hand evaluation needs suits (for
 * flushes), so this is its own self-contained model rather than reusing
 * blackjack's rank-only representation.
 */
public record Card(Rank rank, Suit suit) {
    public enum Rank {
        TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE;

        /** The card's numeric value for comparison (2-14, Ace high). */
        public int value() {
            return ordinal() + 2;
        }
    }

    public enum Suit {
        HEARTS, DIAMONDS, CLUBS, SPADES
    }

    /** Encodes rank+suit into a single int (0-51) for compact network transmission. */
    public int toOrdinal() {
        return rank.ordinal() * Suit.values().length + suit.ordinal();
    }

    public static Card fromOrdinal(int ordinal) {
        Suit[] suits = Suit.values();
        Rank[] ranks = Rank.values();
        return new Card(ranks[ordinal / suits.length], suits[ordinal % suits.length]);
    }
}
