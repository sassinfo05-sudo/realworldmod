package com.realworldmod.npc;

import com.realworldmod.npc.goap.DailyState;

/**
 * Pure mapping from a citizen's {@link DailyState} to where their body
 * should be heading — the missing link between the slice-1 schedule FSM
 * (which only ever updated a database column) and the slice-20 entity
 * (which only ever wandered aimlessly). See {@link CommuteGoal}.
 */
public final class CommuteTarget {
    public enum Destination { HOME, WORKPLACE, NONE }

    private CommuteTarget() {
    }

    public static Destination destinationFor(DailyState state) {
        return switch (state) {
            case SLEEPING, WAKING, COMMUTING_HOME -> Destination.HOME;
            case COMMUTING_TO_WORK, WORKING -> Destination.WORKPLACE;
            case LEISURE -> Destination.NONE;
        };
    }
}
