package com.realworldmod.crime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WantedLevelMathTest {
    @Test
    void increaseAddsToLevel() {
        assertEquals(3, WantedLevelMath.increase(1, 2));
    }

    @Test
    void increaseClampsAtMax() {
        assertEquals(WantedLevelMath.MAX, WantedLevelMath.increase(WantedLevelMath.MAX, 10));
    }

    @Test
    void decaySubtractsFromLevel() {
        assertEquals(1, WantedLevelMath.decay(3, 2));
    }

    @Test
    void decayClampsAtMin() {
        assertEquals(WantedLevelMath.MIN, WantedLevelMath.decay(1, 10));
    }
}
