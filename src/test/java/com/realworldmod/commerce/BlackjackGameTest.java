package com.realworldmod.commerce;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import static com.realworldmod.commerce.BlackjackGame.Rank.ACE;
import static com.realworldmod.commerce.BlackjackGame.Rank.EIGHT;
import static com.realworldmod.commerce.BlackjackGame.Rank.FIVE;
import static com.realworldmod.commerce.BlackjackGame.Rank.KING;
import static com.realworldmod.commerce.BlackjackGame.Rank.NINE;
import static com.realworldmod.commerce.BlackjackGame.Rank.SIX;
import static com.realworldmod.commerce.BlackjackGame.Rank.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlackjackGameTest {
    @Test
    void acePlusKingIsTwentyOneNotTwelve() {
        assertEquals(21, BlackjackGame.valueOf(List.of(ACE, KING)));
    }

    @Test
    void acePlusAceIsTwelveNotTwentyTwo() {
        assertEquals(12, BlackjackGame.valueOf(List.of(ACE, ACE)));
    }

    @Test
    void aceDemotesOnlyAsMuchAsNeededToAvoidBusting() {
        // Ace + 6 + 9 = 11 + 6 + 9 = 26 with the ace at 11, so it demotes to 1 -> 16.
        assertEquals(16, BlackjackGame.valueOf(List.of(ACE, SIX, NINE)));
    }

    @Test
    void plainNumberAndFaceCardsSumNormally() {
        assertEquals(18, BlackjackGame.valueOf(List.of(EIGHT, TEN)));
    }

    @Test
    void dealBustlessHandsAreNotResolved() {
        BlackjackGame game = BlackjackGame.deal(new Random(7));
        // Extremely unlikely this exact seed deals a natural; assert the general contract instead.
        if (BlackjackGame.valueOf(game.playerHand()) != 21) {
            assertTrue(!game.isResolved());
        }
    }

    @Test
    void hittingPastTwentyOneBustsAndResolvesImmediately() {
        BlackjackGame game = deterministicGame(List.of(TEN, NINE), List.of(FIVE, FIVE));
        game.hit(fixedDraws(KING));
        assertTrue(game.isResolved());
        assertEquals(BlackjackGame.Outcome.DEALER_WIN, game.outcome());
    }

    @Test
    void standingMakesTheDealerDrawUntilAtLeastSeventeen() {
        BlackjackGame game = deterministicGame(List.of(TEN, NINE), List.of(FIVE, FIVE));
        BlackjackGame.Outcome outcome = game.stand(fixedDraws(SIX));
        assertTrue(BlackjackGame.valueOf(game.dealerHand()) >= 17);
        assertEquals(BlackjackGame.Outcome.PLAYER_WIN, outcome);
    }

    @Test
    void standingDoesNotDrawWhenDealerAlreadyHasSeventeenOrMore() {
        BlackjackGame game = deterministicGame(List.of(TEN, SIX), List.of(TEN, NINE));
        game.stand(fixedDraws(ACE));
        assertEquals(2, game.dealerHand().size());
    }

    @Test
    void naturalBlackjackPaysThreeToTwo() {
        assertEquals(2.5, BlackjackGame.payoutMultiplier(BlackjackGame.Outcome.PLAYER_BLACKJACK));
    }

    @Test
    void plainWinPaysEvenMoney() {
        assertEquals(2.0, BlackjackGame.payoutMultiplier(BlackjackGame.Outcome.PLAYER_WIN));
    }

    @Test
    void pushReturnsExactlyTheBet() {
        assertEquals(1.0, BlackjackGame.payoutMultiplier(BlackjackGame.Outcome.PUSH));
    }

    @Test
    void dealerWinPaysNothing() {
        assertEquals(0.0, BlackjackGame.payoutMultiplier(BlackjackGame.Outcome.DEALER_WIN));
    }

    @Test
    void hittingAfterResolutionThrows() {
        BlackjackGame game = deterministicGame(List.of(ACE, KING), List.of(FIVE, FIVE));
        assertTrue(game.isResolved());
        assertThrows(IllegalStateException.class, () -> game.hit(fixedDraws(TEN)));
    }

    @Test
    void standingAfterResolutionThrows() {
        BlackjackGame game = deterministicGame(List.of(ACE, KING), List.of(FIVE, FIVE));
        assertThrows(IllegalStateException.class, () -> game.stand(fixedDraws(TEN)));
    }

    @Test
    void outcomeBeforeResolutionThrows() {
        BlackjackGame game = deterministicGame(List.of(TEN, NINE), List.of(FIVE, FIVE));
        assertThrows(IllegalStateException.class, game::outcome);
    }

    /** Builds a game with an exact starting hand by dealing with a rigged draw sequence, bypassing real randomness for deterministic assertions. */
    private static BlackjackGame deterministicGame(List<BlackjackGame.Rank> player, List<BlackjackGame.Rank> dealer) {
        Deque<BlackjackGame.Rank> sequence = new ArrayDeque<>();
        sequence.add(player.get(0));
        sequence.add(dealer.get(0));
        sequence.add(player.get(1));
        sequence.add(dealer.get(1));
        return BlackjackGame.deal(new RiggedRandom(sequence));
    }

    private static Random fixedDraws(BlackjackGame.Rank... ranks) {
        Deque<BlackjackGame.Rank> sequence = new ArrayDeque<>(List.of(ranks));
        return new RiggedRandom(sequence);
    }

    /** A Random stand-in that always returns the index of the next rigged rank instead of a real random value. */
    private static final class RiggedRandom extends Random {
        private final Deque<BlackjackGame.Rank> sequence;

        RiggedRandom(Deque<BlackjackGame.Rank> sequence) {
            this.sequence = sequence;
        }

        @Override
        public int nextInt(int bound) {
            BlackjackGame.Rank next = sequence.isEmpty() ? BlackjackGame.Rank.TWO : sequence.poll();
            return next.ordinal();
        }
    }
}
