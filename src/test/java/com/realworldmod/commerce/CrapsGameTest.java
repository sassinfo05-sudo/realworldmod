package com.realworldmod.commerce;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrapsGameTest {
    @Test
    void freshGameStartsInComeOutPhase() {
        CrapsGame game = CrapsGame.start();
        assertTrue(game.isComeOutPhase());
        assertFalse(game.isResolved());
    }

    @Test
    void comeOutSevenIsANaturalWin() {
        CrapsGame game = CrapsGame.start();
        int total = game.roll(diceTotaling(3, 4));
        assertEquals(7, total);
        assertTrue(game.isResolved());
        assertEquals(CrapsGame.Outcome.PASS_WIN, game.outcome());
    }

    @Test
    void comeOutElevenIsANaturalWin() {
        CrapsGame game = CrapsGame.start();
        game.roll(diceTotaling(5, 6));
        assertTrue(game.isResolved());
        assertEquals(CrapsGame.Outcome.PASS_WIN, game.outcome());
    }

    @Test
    void comeOutTwoIsCrapsAndLoses() {
        CrapsGame game = CrapsGame.start();
        game.roll(diceTotaling(1, 1));
        assertTrue(game.isResolved());
        assertEquals(CrapsGame.Outcome.PASS_LOSE, game.outcome());
    }

    @Test
    void comeOutThreeIsCrapsAndLoses() {
        CrapsGame game = CrapsGame.start();
        game.roll(diceTotaling(1, 2));
        assertTrue(game.isResolved());
        assertEquals(CrapsGame.Outcome.PASS_LOSE, game.outcome());
    }

    @Test
    void comeOutTwelveIsCrapsAndLoses() {
        CrapsGame game = CrapsGame.start();
        game.roll(diceTotaling(6, 6));
        assertTrue(game.isResolved());
        assertEquals(CrapsGame.Outcome.PASS_LOSE, game.outcome());
    }

    @Test
    void comeOutFourEstablishesThePointWithoutResolving() {
        CrapsGame game = CrapsGame.start();
        game.roll(diceTotaling(2, 2));
        assertFalse(game.isResolved());
        assertFalse(game.isComeOutPhase());
        assertEquals(4, game.point());
    }

    @Test
    void repeatingThePointWinsThePassLine() {
        CrapsGame game = CrapsGame.start();
        game.roll(diceTotaling(2, 2));
        game.roll(diceTotaling(3, 3));
        game.roll(diceTotaling(2, 2));
        assertTrue(game.isResolved());
        assertEquals(CrapsGame.Outcome.PASS_WIN, game.outcome());
    }

    @Test
    void rollingASevenAfterAPointIsEstablishedSevensOutAndLoses() {
        CrapsGame game = CrapsGame.start();
        game.roll(diceTotaling(2, 2));
        game.roll(diceTotaling(3, 4));
        assertTrue(game.isResolved());
        assertEquals(CrapsGame.Outcome.PASS_LOSE, game.outcome());
    }

    @Test
    void nonResolvingRollsAfterAPointKeepTheRoundGoing() {
        CrapsGame game = CrapsGame.start();
        game.roll(diceTotaling(2, 2));
        game.roll(diceTotaling(1, 1));
        assertFalse(game.isResolved());
        assertEquals(4, game.point());
    }

    @Test
    void rollingAfterResolutionThrows() {
        CrapsGame game = CrapsGame.start();
        game.roll(diceTotaling(3, 4));
        assertThrows(IllegalStateException.class, () -> game.roll(diceTotaling(3, 4)));
    }

    @Test
    void passWinPaysEvenMoney() {
        assertEquals(2.0, CrapsGame.payoutMultiplier(CrapsGame.Outcome.PASS_WIN));
    }

    @Test
    void passLosePaysNothing() {
        assertEquals(0.0, CrapsGame.payoutMultiplier(CrapsGame.Outcome.PASS_LOSE));
    }

    /** A Random stand-in that produces exactly the given die faces in order, regardless of the requested bound. */
    private static Random diceTotaling(int die1, int die2) {
        Deque<Integer> sequence = new ArrayDeque<>();
        sequence.add(die1 - 1);
        sequence.add(die2 - 1);
        return new RiggedRandom(sequence);
    }

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
