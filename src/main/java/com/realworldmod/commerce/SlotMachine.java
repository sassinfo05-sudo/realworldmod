package com.realworldmod.commerce;

import java.util.Random;

/**
 * Pure slot-machine game logic (Section 6's "casino that actually
 * functions end-to-end" gap): three independently-spun reels over a fixed
 * symbol set, and a real payout table — not a placeholder "casino
 * category" with no actual game underneath.
 */
public final class SlotMachine {
    public enum Symbol {
        CHERRY, BELL, BAR, SEVEN
    }

    public record Spin(Symbol first, Symbol second, Symbol third) {
    }

    private SlotMachine() {
    }

    public static Spin spin(Random random) {
        Symbol[] symbols = Symbol.values();
        return new Spin(
                symbols[random.nextInt(symbols.length)],
                symbols[random.nextInt(symbols.length)],
                symbols[random.nextInt(symbols.length)]);
    }

    /**
     * The payout multiplier for a spin: {@code 0} loses the bet entirely,
     * {@code 1} is a push (the bet is simply returned), anything higher is
     * a real win against the bet.
     */
    public static int payoutMultiplier(Spin spin) {
        if (spin.first() == spin.second() && spin.second() == spin.third()) {
            return switch (spin.first()) {
                case SEVEN -> 10;
                case BAR -> 5;
                case BELL -> 3;
                case CHERRY -> 2;
            };
        }
        if (spin.first() == spin.second() || spin.second() == spin.third() || spin.first() == spin.third()) {
            return 1;
        }
        return 0;
    }
}
