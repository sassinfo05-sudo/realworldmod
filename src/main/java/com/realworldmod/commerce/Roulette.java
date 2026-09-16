package com.realworldmod.commerce;

import java.util.Random;
import java.util.Set;

/**
 * Pure American-wheel roulette logic (38 pockets: {@code 0}, {@code 00},
 * and {@code 1}-{@code 36} with the standard red/black assignment) —
 * Section 6's casino gap, a second real game alongside {@link
 * SlotMachine} rather than the whole floor being one slot machine.
 */
public final class Roulette {
    public enum Color {
        RED, BLACK, GREEN
    }

    public record Pocket(String label, Color color) {
    }

    private static final Set<Integer> RED_NUMBERS = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36);

    private Roulette() {
    }

    /** One of 38 pockets: index 0 = "0", index 37 = "00", 1-36 colored per the standard wheel. */
    public static Pocket spin(Random random) {
        int index = random.nextInt(38);
        if (index == 0) {
            return new Pocket("0", Color.GREEN);
        }
        if (index == 37) {
            return new Pocket("00", Color.GREEN);
        }
        return new Pocket(Integer.toString(index), RED_NUMBERS.contains(index) ? Color.RED : Color.BLACK);
    }

    /** A color bet never wins against green (0 or 00) — the house's actual edge on this bet type. */
    public static boolean colorBetWins(Pocket pocket, Color betColor) {
        return betColor != Color.GREEN && pocket.color() == betColor;
    }
}
