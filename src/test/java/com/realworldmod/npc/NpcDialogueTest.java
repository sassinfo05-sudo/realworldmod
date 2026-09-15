package com.realworldmod.npc;

import com.realworldmod.npc.goap.DailyState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NpcDialogueTest {
    private NpcProfile profileInState(DailyState state) {
        return new NpcProfile(UUID.randomUUID(), "Alex Rivera", "12 Maple St", "Downtown Diner",
                150_000L, 9, 17, state);
    }

    @Test
    void greetingIncludesTheNpcsName() {
        assertTrue(NpcDialogue.greeting(profileInState(DailyState.LEISURE)).startsWith("Alex Rivera:"));
    }

    @Test
    void everyDailyStateHasADistinctLine() {
        var lines = new java.util.HashSet<String>();
        for (DailyState state : DailyState.values()) {
            lines.add(NpcDialogue.greeting(profileInState(state)));
        }
        assertEquals(DailyState.values().length, lines.size());
    }
}
