package com.realworldmod.commerce;

import java.util.Random;

/**
 * Pure craps engine (Section 6's casino gap: a fifth real game alongside
 * {@link SlotMachine}/{@link Roulette}/{@link BlackjackGame}/
 * {@link ThreeCardPokerGame}), implementing the real Pass Line bet — the
 * simplest and most iconic craps wager, not a simplified stand-in: a
 * come-out roll of 7 or 11 wins immediately (a "natural"), 2, 3, or 12
 * loses immediately ("craps"), and any other total establishes "the
 * point" — the shooter then keeps rolling until either the point repeats
 * (a win) or a 7 shows first ("seven out", a loss). Deliberately scoped to
 * the Pass Line alone: no Come/Don't Pass/Don't Come bets, no odds bets,
 * no proposition bets (Field, Hardways, Any Craps) — those are real
 * betting options a full craps table offers, but the Pass Line is the
 * game's own core structure, not a corner cut from it.
 */
public final class CrapsGame {
    public enum Outcome {
        PENDING, PASS_WIN, PASS_LOSE
    }

    private int point = -1;
    private int lastRollTotal = -1;
    private boolean resolved = false;
    private Outcome outcome = Outcome.PENDING;

    private CrapsGame() {
    }

    public static CrapsGame start() {
        return new CrapsGame();
    }

    /** True during the come-out phase, before a point has been established. */
    public boolean isComeOutPhase() {
        return point == -1;
    }

    public int point() {
        return point;
    }

    public int lastRollTotal() {
        return lastRollTotal;
    }

    public boolean isResolved() {
        return resolved;
    }

    public Outcome outcome() {
        return outcome;
    }

    /** Rolls two dice and advances the game according to the real Pass Line rules. Returns the total rolled. */
    public int roll(Random random) {
        if (resolved) {
            throw new IllegalStateException("Round already resolved");
        }

        int total = (1 + random.nextInt(6)) + (1 + random.nextInt(6));
        lastRollTotal = total;

        if (isComeOutPhase()) {
            if (total == 7 || total == 11) {
                resolved = true;
                outcome = Outcome.PASS_WIN;
            } else if (total == 2 || total == 3 || total == 12) {
                resolved = true;
                outcome = Outcome.PASS_LOSE;
            } else {
                point = total;
            }
        } else {
            if (total == point) {
                resolved = true;
                outcome = Outcome.PASS_WIN;
            } else if (total == 7) {
                resolved = true;
                outcome = Outcome.PASS_LOSE;
            }
        }
        return total;
    }

    /** The Pass Line's real payout: even money on a win, nothing on a loss. */
    public static double payoutMultiplier(Outcome outcome) {
        return switch (outcome) {
            case PASS_WIN -> 2.0;
            case PASS_LOSE, PENDING -> 0.0;
        };
    }
}
