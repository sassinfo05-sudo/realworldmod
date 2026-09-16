package com.realworldmod.commerce;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pure blackjack engine (Section 6's casino gap, a third real game
 * alongside {@link SlotMachine}/{@link Roulette}): real hand values (Aces
 * count as 11 unless that would bust the hand, in which case they drop to
 * 1 — the actual rule, not a fixed value), a dealer that hits on any total
 * below 17 and stands otherwise, and a real outcome table including a 3:2
 * natural-blackjack payout. Draws are uniformly random with replacement
 * (an infinite multi-deck shoe) rather than modeling a finite 52-card
 * deck without replacement — a deliberate simplification since nothing
 * in this mod does card-counting-relevant deck tracking.
 */
public final class BlackjackGame {
    public enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10),
        JACK(10), QUEEN(10), KING(10), ACE(11);

        private final int baseValue;

        Rank(int baseValue) {
            this.baseValue = baseValue;
        }

        public int baseValue() {
            return baseValue;
        }
    }

    public enum Outcome {
        PLAYER_BLACKJACK, PLAYER_WIN, DEALER_WIN, PUSH
    }

    private final List<Rank> playerHand = new ArrayList<>();
    private final List<Rank> dealerHand = new ArrayList<>();
    private boolean resolved = false;

    private BlackjackGame() {
    }

    /** Deals the opening two cards to each side; resolves immediately if the player draws a natural blackjack. */
    public static BlackjackGame deal(Random random) {
        BlackjackGame game = new BlackjackGame();
        game.playerHand.add(draw(random));
        game.dealerHand.add(draw(random));
        game.playerHand.add(draw(random));
        game.dealerHand.add(draw(random));
        if (valueOf(game.playerHand) == 21) {
            game.resolved = true;
        }
        return game;
    }

    private static Rank draw(Random random) {
        Rank[] ranks = Rank.values();
        return ranks[random.nextInt(ranks.length)];
    }

    public List<Rank> playerHand() {
        return List.copyOf(playerHand);
    }

    public List<Rank> dealerHand() {
        return List.copyOf(dealerHand);
    }

    public boolean isResolved() {
        return resolved;
    }

    /** The best hand value with Aces counted as 11 unless that would bust, matching real blackjack scoring. */
    public static int valueOf(List<Rank> hand) {
        int total = 0;
        int aces = 0;
        for (Rank rank : hand) {
            total += rank.baseValue();
            if (rank == Rank.ACE) {
                aces++;
            }
        }
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }

    /** Draws one more card for the player; busting resolves the round immediately without the dealer playing. */
    public void hit(Random random) {
        if (resolved) {
            throw new IllegalStateException("Cannot hit once the round is resolved");
        }
        playerHand.add(draw(random));
        if (valueOf(playerHand) > 21) {
            resolved = true;
        }
    }

    /** Player stands: the dealer draws until at least 17, then the round resolves. */
    public Outcome stand(Random random) {
        if (resolved) {
            throw new IllegalStateException("Round already resolved");
        }
        while (valueOf(dealerHand) < 17) {
            dealerHand.add(draw(random));
        }
        resolved = true;
        return outcome();
    }

    public Outcome outcome() {
        if (!resolved) {
            throw new IllegalStateException("Round not resolved yet");
        }
        int playerValue = valueOf(playerHand);
        if (playerValue > 21) {
            return Outcome.DEALER_WIN;
        }

        boolean playerBlackjack = playerHand.size() == 2 && playerValue == 21;
        boolean dealerBlackjack = dealerHand.size() == 2 && valueOf(dealerHand) == 21;
        if (playerBlackjack && dealerBlackjack) {
            return Outcome.PUSH;
        }
        if (playerBlackjack) {
            return Outcome.PLAYER_BLACKJACK;
        }
        if (dealerBlackjack) {
            return Outcome.DEALER_WIN;
        }

        int dealerValue = valueOf(dealerHand);
        if (dealerValue > 21) {
            return Outcome.PLAYER_WIN;
        }
        if (playerValue > dealerValue) {
            return Outcome.PLAYER_WIN;
        }
        if (playerValue < dealerValue) {
            return Outcome.DEALER_WIN;
        }
        return Outcome.PUSH;
    }

    /** The real payout multiplier for a resolved outcome: 3:2 on a natural, even money on a plain win, bet back on a push, nothing on a loss. */
    public static double payoutMultiplier(Outcome outcome) {
        return switch (outcome) {
            case PLAYER_BLACKJACK -> 2.5;
            case PLAYER_WIN -> 2.0;
            case PUSH -> 1.0;
            case DEALER_WIN -> 0.0;
        };
    }
}
