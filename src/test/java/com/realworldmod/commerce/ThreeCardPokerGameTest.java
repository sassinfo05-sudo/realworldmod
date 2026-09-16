package com.realworldmod.commerce;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import static com.realworldmod.commerce.Card.Rank.ACE;
import static com.realworldmod.commerce.Card.Rank.FIVE;
import static com.realworldmod.commerce.Card.Rank.FOUR;
import static com.realworldmod.commerce.Card.Rank.JACK;
import static com.realworldmod.commerce.Card.Rank.KING;
import static com.realworldmod.commerce.Card.Rank.QUEEN;
import static com.realworldmod.commerce.Card.Rank.SEVEN;
import static com.realworldmod.commerce.Card.Rank.SIX;
import static com.realworldmod.commerce.Card.Rank.TWO;
import static com.realworldmod.commerce.Card.Suit.CLUBS;
import static com.realworldmod.commerce.Card.Suit.DIAMONDS;
import static com.realworldmod.commerce.Card.Suit.HEARTS;
import static com.realworldmod.commerce.Card.Suit.SPADES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThreeCardPokerGameTest {
    @Test
    void freshlyDealtGameIsNotResolved() {
        ThreeCardPokerGame game = deterministicGame(
                List.of(new Card(KING, HEARTS), new Card(SEVEN, CLUBS), new Card(TWO, SPADES)),
                List.of(new Card(FOUR, HEARTS), new Card(FIVE, CLUBS), new Card(SIX, SPADES)));
        assertTrue(!game.isResolved());
    }

    @Test
    void foldingResolvesWithFoldedOutcomeAndNoPayout() {
        ThreeCardPokerGame game = deterministicGame(
                List.of(new Card(KING, HEARTS), new Card(SEVEN, CLUBS), new Card(TWO, SPADES)),
                List.of(new Card(FOUR, HEARTS), new Card(FIVE, CLUBS), new Card(SIX, SPADES)));
        game.fold();
        assertTrue(game.isResolved());
        assertEquals(ThreeCardPokerGame.Outcome.FOLDED, game.outcome());
        assertEquals(0.0, ThreeCardPokerGame.anteMultiplier(game.outcome()));
    }

    @Test
    void dealerBelowQueenHighDoesNotQualify() {
        // Dealer: Jack-high (below Queen) with no pair/straight/flush.
        ThreeCardPokerGame game = deterministicGame(
                List.of(new Card(TWO, HEARTS), new Card(FOUR, CLUBS), new Card(SIX, SPADES)),
                List.of(new Card(JACK, HEARTS), new Card(SEVEN, CLUBS), new Card(TWO, DIAMONDS)));
        ThreeCardPokerGame.Outcome outcome = game.play();
        assertEquals(ThreeCardPokerGame.Outcome.DEALER_NOT_QUALIFIED, outcome);
        assertEquals(2.0, ThreeCardPokerGame.anteMultiplier(outcome));
        assertEquals(1.0, ThreeCardPokerGame.playMultiplier(outcome));
    }

    @Test
    void dealerQueenHighQualifiesAndBeatsWorsePlayerHand() {
        ThreeCardPokerGame game = deterministicGame(
                List.of(new Card(TWO, HEARTS), new Card(FOUR, CLUBS), new Card(SIX, SPADES)),
                List.of(new Card(QUEEN, HEARTS), new Card(SEVEN, CLUBS), new Card(TWO, DIAMONDS)));
        ThreeCardPokerGame.Outcome outcome = game.play();
        assertEquals(ThreeCardPokerGame.Outcome.DEALER_WIN, outcome);
        assertEquals(0.0, ThreeCardPokerGame.anteMultiplier(outcome));
        assertEquals(0.0, ThreeCardPokerGame.playMultiplier(outcome));
    }

    @Test
    void playerWinsWithABetterQualifiedHand() {
        ThreeCardPokerGame game = deterministicGame(
                List.of(new Card(ACE, HEARTS), new Card(KING, CLUBS), new Card(TWO, SPADES)),
                List.of(new Card(QUEEN, HEARTS), new Card(SEVEN, CLUBS), new Card(TWO, DIAMONDS)));
        ThreeCardPokerGame.Outcome outcome = game.play();
        assertEquals(ThreeCardPokerGame.Outcome.PLAYER_WIN, outcome);
        assertEquals(2.0, ThreeCardPokerGame.anteMultiplier(outcome));
        assertEquals(2.0, ThreeCardPokerGame.playMultiplier(outcome));
    }

    @Test
    void identicalQualifyingHandsPush() {
        ThreeCardPokerGame game = deterministicGame(
                List.of(new Card(KING, HEARTS), new Card(SEVEN, CLUBS), new Card(TWO, SPADES)),
                List.of(new Card(KING, DIAMONDS), new Card(SEVEN, SPADES), new Card(TWO, HEARTS)));
        ThreeCardPokerGame.Outcome outcome = game.play();
        assertEquals(ThreeCardPokerGame.Outcome.PUSH, outcome);
        assertEquals(1.0, ThreeCardPokerGame.anteMultiplier(outcome));
        assertEquals(1.0, ThreeCardPokerGame.playMultiplier(outcome));
    }

    @Test
    void playingAfterResolutionThrows() {
        ThreeCardPokerGame game = deterministicGame(
                List.of(new Card(KING, HEARTS), new Card(SEVEN, CLUBS), new Card(TWO, SPADES)),
                List.of(new Card(FOUR, HEARTS), new Card(FIVE, CLUBS), new Card(SIX, SPADES)));
        game.fold();
        assertThrows(IllegalStateException.class, game::play);
    }

    @Test
    void foldingAfterResolutionThrows() {
        ThreeCardPokerGame game = deterministicGame(
                List.of(new Card(KING, HEARTS), new Card(SEVEN, CLUBS), new Card(TWO, SPADES)),
                List.of(new Card(FOUR, HEARTS), new Card(FIVE, CLUBS), new Card(SIX, SPADES)));
        game.play();
        assertThrows(IllegalStateException.class, game::fold);
    }

    @Test
    void outcomeBeforeResolutionThrows() {
        ThreeCardPokerGame game = deterministicGame(
                List.of(new Card(KING, HEARTS), new Card(SEVEN, CLUBS), new Card(TWO, SPADES)),
                List.of(new Card(FOUR, HEARTS), new Card(FIVE, CLUBS), new Card(SIX, SPADES)));
        assertThrows(IllegalStateException.class, game::outcome);
    }

    /** Deals an exact hand pair via a rigged draw sequence, bypassing real randomness for deterministic assertions. */
    private static ThreeCardPokerGame deterministicGame(List<Card> player, List<Card> dealer) {
        Deque<Integer> sequence = new ArrayDeque<>();
        for (int i = 0; i < 3; i++) {
            enqueueCard(sequence, player.get(i));
            enqueueCard(sequence, dealer.get(i));
        }
        return ThreeCardPokerGame.deal(new RiggedRandom(sequence));
    }

    private static void enqueueCard(Deque<Integer> sequence, Card card) {
        sequence.add(card.rank().ordinal());
        sequence.add(card.suit().ordinal());
    }

    /** A Random stand-in that returns exactly the rigged sequence of nextInt results, ignoring the requested bound. */
    private static final class RiggedRandom extends Random {
        private final Deque<Integer> sequence;

        RiggedRandom(Deque<Integer> sequence) {
            this.sequence = sequence;
        }

        @Override
        public int nextInt(int bound) {
            return sequence.isEmpty() ? 0 : sequence.poll();
        }
    }
}
