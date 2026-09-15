package com.realworldmod.npc;

import com.realworldmod.npc.goap.DailyState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommuteTargetTest {
    @Test
    void sleepingWakingAndCommutingHomeTargetHome() {
        assertEquals(CommuteTarget.Destination.HOME, CommuteTarget.destinationFor(DailyState.SLEEPING));
        assertEquals(CommuteTarget.Destination.HOME, CommuteTarget.destinationFor(DailyState.WAKING));
        assertEquals(CommuteTarget.Destination.HOME, CommuteTarget.destinationFor(DailyState.COMMUTING_HOME));
    }

    @Test
    void commutingToWorkAndWorkingTargetWorkplace() {
        assertEquals(CommuteTarget.Destination.WORKPLACE, CommuteTarget.destinationFor(DailyState.COMMUTING_TO_WORK));
        assertEquals(CommuteTarget.Destination.WORKPLACE, CommuteTarget.destinationFor(DailyState.WORKING));
    }

    @Test
    void leisureTargetsNoDestination() {
        assertEquals(CommuteTarget.Destination.NONE, CommuteTarget.destinationFor(DailyState.LEISURE));
    }
}
