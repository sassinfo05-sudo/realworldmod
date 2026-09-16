package com.realworldmod.commerce;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pure Three Card Poker engine (Section 6's casino gap: a fourth real game
 * alongside {@link SlotMachine}/{@link Roulette}/{@link BlackjackGame}):
 * real Ante/Play betting (the actual game's structure, not a simplified
 * stand-in), a dealer that must qualify with Queen-high or better, and
 * real hand evaluation via {@link ThreeCardPokerHandEvaluator}. Draws are
 * uniformly random with replacement (an infinite shoe) rather than
 * modeling a finite 52-card deck without replacement — the same
 * deliberate simplification {@link BlackjackGame} already documents.
 */
public final class ThreeCardPokerGame {
    public enum Outcome {
        FOLDED, DEALER_NOT_QUALIFIED, PLAYER_WIN, DEALER_WIN, PUSH
    }

    private final List<Card> playerHand = new ArrayList<>();
    private final List<Card> dealerHand = new ArrayList<>();
    private boolean resolved = false;
    private boolean folded = false;

    private ThreeCardPokerGame() {
    }

    /** Deals three cards to each side. Nothing resolves yet — the player must fold or play. */
    public static ThreeCardPokerGame deal(Random random) {
        ThreeCardPokerGame game = new ThreeCardPokerGame();
        for (int i = 0; i < 3; i++) {
            game.playerHand.add(draw(random));
            game.dealerHand.add(draw(random));
        }
        return game;
    }

    private static Card draw(Random random) {
        Card.Rank[] ranks = Card.Rank.values();
        Card.Suit[] suits = Card.Suit.values();
        return new Card(ranks[random.nextInt(ranks.length)], suits[random.nextInt(suits.length)]);
    }

    public List<Card> playerHand() {
        return List.copyOf(playerHand);
    }

    public List<Card> dealerHand() {
        return List.copyOf(dealerHand);
    }

    public boolean isResolved() {
        return resolved;
    }

    /** Folding forfeits the ante without ever revealing the dealer's hand as "played against". */
    public void fold() {
        if (resolved) {
            throw new IllegalStateException("Round already resolved");
        }
        folded = true;
        resolved = true;
    }

    /** Matches the ante with a play bet, reveals the dealer's hand, and resolves the round. */
    public Outcome play() {
        if (resolved) {
            throw new IllegalStateException("Round already resolved");
        }
        resolved = true;
        return outcome();
    }

    public Outcome outcome() {
        if (!resolved) {
            throw new IllegalStateException("Round not resolved yet");
        }
        if (folded) {
            return Outcome.FOLDED;
        }

        ThreeCardPokerHand dealerEval = ThreeCardPokerHandEvaluator.evaluate(dealerHand);
        if (!dealerQualifies(dealerEval)) {
            return Outcome.DEALER_NOT_QUALIFIED;
        }

        ThreeCardPokerHand playerEval = ThreeCardPokerHandEvaluator.evaluate(playerHand);
        int comparison = playerEval.compareTo(dealerEval);
        if (comparison > 0) {
            return Outcome.PLAYER_WIN;
        }
        if (comparison < 0) {
            return Outcome.DEALER_WIN;
        }
        return Outcome.PUSH;
    }

    /** The real Three Card Poker rule: the dealer needs at least Queen-high to qualify; any pair or better always qualifies. */
    private static boolean dealerQualifies(ThreeCardPokerHand dealerHand) {
        if (dealerHand.rank() != ThreeCardPokerHandRank.HIGH_CARD) {
            return true;
        }
        return dealerHand.tiebreakValuesDescending().get(0) >= Card.Rank.QUEEN.value();
    }

    /** The multiplier applied to the ante bet: paid even money if the dealer doesn't qualify or the player wins, pushed on a tie, lost otherwise. */
    public static double anteMultiplier(Outcome outcome) {
        return switch (outcome) {
            case FOLDED, DEALER_WIN -> 0.0;
            case DEALER_NOT_QUALIFIED, PLAYER_WIN -> 2.0;
            case PUSH -> 1.0;
        };
    }

    /** The multiplier applied to the play bet: pushed if the dealer doesn't qualify (never won or lost), otherwise follows the hand comparison. */
    public static double playMultiplier(Outcome outcome) {
        return switch (outcome) {
            case FOLDED, DEALER_WIN -> 0.0;
            case DEALER_NOT_QUALIFIED, PUSH -> 1.0;
            case PLAYER_WIN -> 2.0;
        };
    }
}
