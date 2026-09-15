package com.realworldmod.npc;

import com.realworldmod.npc.goap.DailyState;

/**
 * Pure formatting for the placeholder greeting shown when a player
 * interacts with a {@code CitizenEntity} (Section 2's "dynamic dialogue
 * interface", reduced to a single fixed line per daily-schedule state for
 * this slice — see ROADMAP.md for the real dialogue-tree gap).
 */
public final class NpcDialogue {
    private NpcDialogue() {
    }

    public static String greeting(NpcProfile profile) {
        return profile.name() + ": \"" + lineFor(profile.currentState()) + "\"";
    }

    private static String lineFor(DailyState state) {
        return switch (state) {
            case SLEEPING -> "Just resting for now.";
            case WAKING -> "Morning already?";
            case COMMUTING_TO_WORK -> "Can't talk, I'll be late for work!";
            case WORKING -> "Busy working right now.";
            case COMMUTING_HOME -> "Heading home for the day.";
            case LEISURE -> "Nice to see you. Just relaxing.";
        };
    }
}
