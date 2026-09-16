package com.realworldmod.npc;

import com.realworldmod.npc.goap.DailyScheduleFSM;
import com.realworldmod.npc.goap.DailyState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DailyScheduleFSMTest {
    private static final int WORK_START = 9;
    private static final int WORK_END = 17;

    @Test
    void staysAsleepDuringTheNight() {
        assertEquals(DailyState.SLEEPING,
                DailyScheduleFSM.nextState(DailyState.SLEEPING, 3, WORK_START, WORK_END));
    }

    @Test
    void wakesUpInTheMorning() {
        assertEquals(DailyState.WAKING,
                DailyScheduleFSM.nextState(DailyState.SLEEPING, 7, WORK_START, WORK_END));
    }

    @Test
    void commutesToWorkJustBeforeShift() {
        assertEquals(DailyState.COMMUTING_TO_WORK,
                DailyScheduleFSM.nextState(DailyState.WAKING, 8, WORK_START, WORK_END));
    }

    @Test
    void arrivesAtWorkOnceShiftStarts() {
        assertEquals(DailyState.WORKING,
                DailyScheduleFSM.nextState(DailyState.COMMUTING_TO_WORK, 9, WORK_START, WORK_END));
    }

    @Test
    void staysAtWorkDuringShift() {
        assertEquals(DailyState.WORKING,
                DailyScheduleFSM.nextState(DailyState.WORKING, 12, WORK_START, WORK_END));
    }

    @Test
    void headsHomeAfterShiftEnds() {
        assertEquals(DailyState.COMMUTING_HOME,
                DailyScheduleFSM.nextState(DailyState.WORKING, 17, WORK_START, WORK_END));
    }

    @Test
    void relaxesInTheEvening() {
        assertEquals(DailyState.LEISURE,
                DailyScheduleFSM.nextState(DailyState.COMMUTING_HOME, 19, WORK_START, WORK_END));
    }

    @Test
    void goesToBedLateAtNight() {
        assertEquals(DailyState.SLEEPING,
                DailyScheduleFSM.nextState(DailyState.LEISURE, 23, WORK_START, WORK_END));
    }

    @Test
    void supportsOvernightShiftsThatWrapMidnight() {
        int nightStart = 22;
        int nightEnd = 6;
        assertEquals(DailyState.WORKING,
                DailyScheduleFSM.nextState(DailyState.WORKING, 2, nightStart, nightEnd));
        assertEquals(DailyState.COMMUTING_HOME,
                DailyScheduleFSM.nextState(DailyState.WORKING, 6, nightStart, nightEnd));
    }
}
