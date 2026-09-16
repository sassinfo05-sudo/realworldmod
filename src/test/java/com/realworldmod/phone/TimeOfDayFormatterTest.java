package com.realworldmod.phone;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeOfDayFormatterTest {
    @Test
    void ticksZeroIsSixAM() {
        assertEquals("6:00 AM", TimeOfDayFormatter.format(0));
    }

    @Test
    void noonIsTwelvePM() {
        assertEquals("12:00 PM", TimeOfDayFormatter.format(6000));
    }

    @Test
    void midnightIsTwelveAM() {
        assertEquals("12:00 AM", TimeOfDayFormatter.format(18000));
    }

    @Test
    void formatWrapsPastTwentyFourThousandTicks() {
        assertEquals(TimeOfDayFormatter.format(500), TimeOfDayFormatter.format(24500));
    }

    @Test
    void formatHandlesMinutesWithinTheHour() {
        // 6:00 AM + 500 ticks (~30 in-game minutes given 1000 ticks/hour)
        assertEquals("6:30 AM", TimeOfDayFormatter.format(500));
    }
}
