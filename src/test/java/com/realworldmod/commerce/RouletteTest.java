package com.realworldmod.commerce;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouletteTest {
    @Test
    void oneIsRed() {
        assertEquals(Roulette.Color.RED, new Roulette.Pocket("1", Roulette.Color.RED).color());
    }

    @Test
    void zeroAndDoubleZeroAreGreen() {
        Random random = new Random(1);
        boolean sawGreen = false;
        for (int i = 0; i < 500; i++) {
            Roulette.Pocket pocket = Roulette.spin(random);
            if (pocket.label().equals("0") || pocket.label().equals("00")) {
                assertEquals(Roulette.Color.GREEN, pocket.color());
                sawGreen = true;
            }
        }
        assertTrue(sawGreen, "500 spins should have landed on green at least once");
    }

    @Test
    void everyNonGreenPocketIsRedOrBlack() {
        Random random = new Random(2);
        for (int i = 0; i < 500; i++) {
            Roulette.Pocket pocket = Roulette.spin(random);
            if (pocket.color() != Roulette.Color.GREEN) {
                assertTrue(pocket.color() == Roulette.Color.RED || pocket.color() == Roulette.Color.BLACK);
            }
        }
    }

    @Test
    void spinCanProduceBothColorsAndGreenAcrossManyTries() {
        Random random = new Random(3);
        Set<Roulette.Color> seen = EnumSet.noneOf(Roulette.Color.class);
        for (int i = 0; i < 500; i++) {
            seen.add(Roulette.spin(random).color());
        }
        assertEquals(EnumSet.allOf(Roulette.Color.class), seen);
    }

    @Test
    void matchingColorBetWins() {
        Roulette.Pocket redPocket = new Roulette.Pocket("1", Roulette.Color.RED);
        assertTrue(Roulette.colorBetWins(redPocket, Roulette.Color.RED));
    }

    @Test
    void mismatchedColorBetLoses() {
        Roulette.Pocket redPocket = new Roulette.Pocket("1", Roulette.Color.RED);
        assertFalse(Roulette.colorBetWins(redPocket, Roulette.Color.BLACK));
    }

    @Test
    void greenAlwaysLosesAColorBetEvenIfSomehowBetOnGreen() {
        Roulette.Pocket greenPocket = new Roulette.Pocket("0", Roulette.Color.GREEN);
        assertFalse(Roulette.colorBetWins(greenPocket, Roulette.Color.GREEN));
        assertFalse(Roulette.colorBetWins(greenPocket, Roulette.Color.RED));
        assertFalse(Roulette.colorBetWins(greenPocket, Roulette.Color.BLACK));
    }
}
