package com.realworldmod.commerce;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import static com.realworldmod.commerce.SlotMachine.Symbol.BAR;
import static com.realworldmod.commerce.SlotMachine.Symbol.BELL;
import static com.realworldmod.commerce.SlotMachine.Symbol.CHERRY;
import static com.realworldmod.commerce.SlotMachine.Symbol.SEVEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotMachineTest {
    @Test
    void threeSevensPaysTenTimes() {
        assertEquals(10, SlotMachine.payoutMultiplier(new SlotMachine.Spin(SEVEN, SEVEN, SEVEN)));
    }

    @Test
    void threeBarsPaysFiveTimes() {
        assertEquals(5, SlotMachine.payoutMultiplier(new SlotMachine.Spin(BAR, BAR, BAR)));
    }

    @Test
    void threeBellsPaysThreeTimes() {
        assertEquals(3, SlotMachine.payoutMultiplier(new SlotMachine.Spin(BELL, BELL, BELL)));
    }

    @Test
    void threeCherriesPaysTwoTimes() {
        assertEquals(2, SlotMachine.payoutMultiplier(new SlotMachine.Spin(CHERRY, CHERRY, CHERRY)));
    }

    @Test
    void twoMatchingIsAPush() {
        assertEquals(1, SlotMachine.payoutMultiplier(new SlotMachine.Spin(SEVEN, SEVEN, BAR)));
        assertEquals(1, SlotMachine.payoutMultiplier(new SlotMachine.Spin(BAR, SEVEN, SEVEN)));
        assertEquals(1, SlotMachine.payoutMultiplier(new SlotMachine.Spin(SEVEN, BAR, SEVEN)));
    }

    @Test
    void noMatchLosesTheBet() {
        assertEquals(0, SlotMachine.payoutMultiplier(new SlotMachine.Spin(SEVEN, BAR, BELL)));
    }

    @Test
    void spinCanProduceEverySymbolAcrossManyTries() {
        Random random = new Random(42);
        Set<SlotMachine.Symbol> seen = EnumSet.noneOf(SlotMachine.Symbol.class);
        for (int i = 0; i < 200; i++) {
            SlotMachine.Spin spin = SlotMachine.spin(random);
            seen.add(spin.first());
            seen.add(spin.second());
            seen.add(spin.third());
        }
        assertTrue(seen.containsAll(EnumSet.allOf(SlotMachine.Symbol.class)));
    }
}
